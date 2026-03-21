package com.daremote.app.feature.terminal

import android.content.Context
import android.os.Environment
import android.view.KeyEvent
import android.view.MotionEvent
import android.view.inputmethod.InputMethodManager
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.daremote.app.core.data.ssh.SftpClient
import com.daremote.app.core.data.ssh.SshSessionManager
import com.daremote.app.core.domain.model.Snippet
import com.daremote.app.core.domain.repository.ServerRepository
import com.daremote.app.core.domain.repository.SnippetRepository
import com.termux.terminal.TerminalSession
import com.termux.terminal.TerminalSessionClient
import com.termux.view.TerminalView
import com.termux.view.TerminalViewClient
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import net.schmizz.sshj.connection.channel.direct.Session
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.io.OutputStream
import java.lang.ref.WeakReference
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream
import javax.inject.Inject

enum class TerminalPanel { TERMINAL, SFTP, SNIPPETS }

// ── Dual-pane file manager data model ─────────────────────────────────────────

data class FileEntry(
    val name: String,
    val path: String,
    val size: Long,
    val isDirectory: Boolean,
    val modifiedAt: Long
)

data class PaneState(
    val isLocal: Boolean,
    val path: String,
    val files: List<FileEntry> = emptyList(),
    val selectedPaths: Set<String> = emptySet(),
    val isLoading: Boolean = false,
    val error: String? = null
)

data class DualPaneState(
    val leftPane: PaneState = PaneState(isLocal = true, path = "/sdcard"),
    val rightPane: PaneState = PaneState(isLocal = false, path = "/"),
    val activePaneIsLeft: Boolean = true,
    val isTransferring: Boolean = false,
    val transferProgress: Float? = null,
    val operationLabel: String = "",
    val operationError: String? = null
)

// ── Terminal session model ────────────────────────────────────────────────────

data class TerminalSessionData(
    val id: Int,
    val name: String,
    val isConnected: Boolean = false,
    val terminalSession: TerminalSession? = null
)

data class TerminalState(
    val sessions: List<TerminalSessionData> = emptyList(),
    val currentSessionId: Int = 0,
    val serverName: String = "",
    val isConnected: Boolean = false,
    val error: String? = null,
    val activePanel: TerminalPanel = TerminalPanel.TERMINAL,
    val snippets: List<Snippet> = emptyList(),
    val dualPane: DualPaneState = DualPaneState()
)

// ── ViewModel ─────────────────────────────────────────────────────────────────

@HiltViewModel
class TerminalViewModel @Inject constructor(
    private val sessionManager: SshSessionManager,
    private val serverRepository: ServerRepository,
    private val snippetRepository: SnippetRepository,
    private val sftpClient: SftpClient,
    savedStateHandle: SavedStateHandle,
    @ApplicationContext private val context: Context
) : ViewModel(), TerminalSessionClient, TerminalViewClient {

    private val serverId: Long = savedStateHandle["serverId"] ?: 0L
    private val _state = MutableStateFlow(TerminalState())
    val state: StateFlow<TerminalState> = _state

    private var sessionCounter = 0
    private val shellMap = mutableMapOf<Int, Pair<Session.Shell, OutputStream>>()
    private var terminalViewRef: WeakReference<TerminalView>? = null

    private val leftPathStack = mutableListOf<String>()
    private val rightPathStack = mutableListOf<String>()

    private val localRoot: String by lazy {
        try { Environment.getExternalStorageDirectory().absolutePath }
        catch (_: Exception) { context.filesDir.absolutePath }
    }

    init {
        // SSH connection + first session
        viewModelScope.launch {
            try {
                val server = serverRepository.getServerById(serverId)
                if (server == null) {
                    _state.update { it.copy(error = "Server not found") }
                    return@launch
                }
                _state.update { it.copy(serverName = server.name) }
                if (!sessionManager.isConnected(serverId)) sessionManager.connect(server)
                _state.update { it.copy(isConnected = true) }
                openNewSession()
            } catch (e: Exception) {
                _state.update { it.copy(error = "Connection failed: ${e.message}") }
            }
        }

        // Snippets
        viewModelScope.launch {
            snippetRepository.getAllSnippets().collect { snippets ->
                _state.update { it.copy(snippets = snippets) }
            }
        }

        // Init dual pane paths
        leftPathStack.add(localRoot)
        rightPathStack.add("/")
        _state.update {
            it.copy(
                dualPane = DualPaneState(
                    leftPane = PaneState(isLocal = true, path = localRoot),
                    rightPane = PaneState(isLocal = false, path = "/")
                )
            )
        }
    }

    // ── Terminal session management ───────────────────────────────────────────

    fun setTerminalView(view: TerminalView) { terminalViewRef = WeakReference(view) }

    private fun findShellPath(): String {
        val candidates = listOf(
            "/system/bin/sh", "/system/bin/toybox", "/system/bin/toolbox",
            "/vendor/bin/sh", "/system/xbin/sh"
        )
        return candidates.firstOrNull { File(it).canExecute() } ?: "/system/bin/sh"
    }

    fun openNewSession() {
        viewModelScope.launch {
            try {
                val client = sessionManager.getSession(serverId) ?: run {
                    _state.update { it.copy(error = "SSH client session not available") }
                    return@launch
                }
                val (_, shell) = withContext(Dispatchers.IO) {
                    val session = client.startSession()
                    session.allocatePTY("xterm-256color", 80, 24, 0, 0, emptyMap())
                    session to session.startShell()
                }
                val termSession = withContext(Dispatchers.Main) {
                    val shellPath = findShellPath()
                    TerminalSession(
                        shellPath, context.filesDir.absolutePath, arrayOf(shellPath),
                        arrayOf("TERM=xterm-256color", "HOME=${context.filesDir.absolutePath}"),
                        10000, this@TerminalViewModel
                    ).also { try { it.initializeEmulator(80, 24) } catch (_: Exception) {} }
                }
                val sessionId = sessionCounter++
                shellMap[sessionId] = shell to shell.outputStream
                _state.update { s ->
                    s.copy(
                        sessions = s.sessions + TerminalSessionData(sessionId, "Session ${sessionId + 1}", true, termSession),
                        currentSessionId = sessionId
                    )
                }
                readShellOutput(sessionId, shell.inputStream, termSession)
            } catch (e: Exception) {
                _state.update { it.copy(error = "Failed to start shell: ${e.message}") }
            }
        }
    }

    fun switchSession(sessionId: Int) = _state.update { it.copy(currentSessionId = sessionId) }

    private fun readShellOutput(sessionId: Int, inputStream: InputStream, termSession: TerminalSession) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val buf = ByteArray(16384)
                while (true) {
                    val n = inputStream.read(buf); if (n == -1) break
                    val emulator = termSession.emulator ?: continue
                    emulator.append(buf, n)
                    terminalViewRef?.get()?.let { v -> v.post { v.onScreenUpdated() } }
                }
            } catch (_: Exception) {}
        }
    }

    // ── Panel switching ───────────────────────────────────────────────────────

    fun setActivePanel(panel: TerminalPanel) {
        _state.update { it.copy(activePanel = panel) }
        if (panel == TerminalPanel.SFTP) {
            val dp = _state.value.dualPane
            if (dp.leftPane.files.isEmpty() && !dp.leftPane.isLoading) loadPane(true, dp.leftPane.path)
            if (dp.rightPane.files.isEmpty() && !dp.rightPane.isLoading) loadPane(false, dp.rightPane.path)
        }
    }

    // ── Snippet execution ─────────────────────────────────────────────────────

    fun executeSnippet(snippet: Snippet) {
        sendInput(_state.value.currentSessionId, (snippet.command + "\n").toByteArray())
        viewModelScope.launch { snippetRepository.markUsed(snippet.id) }
        _state.update { it.copy(activePanel = TerminalPanel.TERMINAL) }
    }

    // ── Dual pane navigation ──────────────────────────────────────────────────

    fun setActivePane(isLeft: Boolean) = updateDualPane { it.copy(activePaneIsLeft = isLeft) }

    fun navigateTo(isLeft: Boolean, path: String) {
        val stack = if (isLeft) leftPathStack else rightPathStack
        if (path != stack.lastOrNull()) stack.add(path)
        loadPane(isLeft, path)
    }

    fun navigateUp(isLeft: Boolean) {
        val stack = if (isLeft) leftPathStack else rightPathStack
        if (stack.size <= 1) return
        stack.removeLastOrNull()
        val parent = stack.lastOrNull() ?: "/"
        loadPane(isLeft, parent)
    }

    fun togglePaneIsLocal(isLeft: Boolean) {
        val pane = if (isLeft) _state.value.dualPane.leftPane else _state.value.dualPane.rightPane
        val newIsLocal = !pane.isLocal
        val newPath = if (newIsLocal) localRoot else "/"
        val stack = if (isLeft) leftPathStack else rightPathStack
        stack.clear(); stack.add(newPath)
        updatePane(isLeft) { it.copy(isLocal = newIsLocal, path = newPath, files = emptyList(), selectedPaths = emptySet(), error = null) }
        loadPaneWith(isLeft, newPath, newIsLocal)
    }

    fun toggleSelection(isLeft: Boolean, filePath: String) {
        updatePane(isLeft) { pane ->
            val sel = pane.selectedPaths.toMutableSet()
            if (filePath in sel) sel.remove(filePath) else sel.add(filePath)
            pane.copy(selectedPaths = sel)
        }
    }

    fun selectAll(isLeft: Boolean) {
        updatePane(isLeft) { pane ->
            pane.copy(selectedPaths = pane.files.map { it.path }.toSet())
        }
    }

    fun clearSelection(isLeft: Boolean) = updatePane(isLeft) { it.copy(selectedPaths = emptySet()) }

    fun dismissOperationError() = updateDualPane { it.copy(operationError = null) }

    fun refreshPane(isLeft: Boolean) {
        val pane = if (isLeft) _state.value.dualPane.leftPane else _state.value.dualPane.rightPane
        loadPane(isLeft, pane.path)
    }

    private fun loadPane(isLeft: Boolean, path: String) {
        val isLocal = if (isLeft) _state.value.dualPane.leftPane.isLocal else _state.value.dualPane.rightPane.isLocal
        updatePane(isLeft) { it.copy(path = path, isLoading = true, error = null) }
        loadPaneWith(isLeft, path, isLocal)
    }

    private fun loadPaneWith(isLeft: Boolean, path: String, isLocal: Boolean) {
        viewModelScope.launch {
            try {
                val files = if (isLocal) {
                    withContext(Dispatchers.IO) { listLocalFiles(path) }
                } else {
                    listRemoteFiles(path)
                }
                updatePane(isLeft) { it.copy(files = files, isLoading = false) }
            } catch (e: Exception) {
                updatePane(isLeft) { it.copy(error = e.message, isLoading = false) }
            }
        }
    }

    // ── Dual pane operations ──────────────────────────────────────────────────

    fun copySelected() = runPaneOperation("Copying…") { src, dst, srcIsLeft ->
        val selected = src.files.filter { it.path in src.selectedPaths }
        selected.forEachIndexed { i, file ->
            val base = i.toFloat() / selected.size
            val slot = 1f / selected.size
            copyFile(file, src, dst) { p -> updateDualPane { it.copy(transferProgress = base + p * slot) } }
            updateDualPane { it.copy(transferProgress = (i + 1f) / selected.size) }
        }
        srcIsLeft
    }

    fun moveSelected() = runPaneOperation("Moving…") { src, dst, srcIsLeft ->
        val selected = src.files.filter { it.path in src.selectedPaths }
        selected.forEachIndexed { i, file ->
            val base = i.toFloat() / selected.size
            val slot = 1f / selected.size
            copyFile(file, src, dst) { p -> updateDualPane { it.copy(transferProgress = base + p * slot) } }
            deleteFile(file, src)
            updateDualPane { it.copy(transferProgress = (i + 1f) / selected.size) }
        }
        srcIsLeft
    }

    fun zipSelected(outputName: String) = runPaneOperation("Creating ZIP…") { src, dst, srcIsLeft ->
        val zipName = if (outputName.endsWith(".zip")) outputName else "$outputName.zip"
        val selected = src.files.filter { it.path in src.selectedPaths }
        when {
            src.isLocal && dst.isLocal -> withContext(Dispatchers.IO) {
                createLocalZip(selected, "${dst.path}/$zipName")
            }
            src.isLocal && !dst.isLocal -> withContext(Dispatchers.IO) {
                val tmp = File(context.cacheDir, zipName)
                createLocalZip(selected, tmp.absolutePath)
                sftpClient.upload(serverId, tmp.absolutePath, "${dst.path}/$zipName")
                tmp.delete()
            }
            else -> {
                // Remote source: run zip on server, output to dst path
                val files = selected.joinToString(" ") { "'${it.path}'" }
                val outPath = if (dst.isLocal) "${dst.path}/$zipName" else "${dst.path}/$zipName"
                runRemoteCmd("zip -r '$outPath' $files")
            }
        }
        srcIsLeft
    }

    fun tarSelected(outputName: String) = runPaneOperation("Creating TAR…") { src, dst, srcIsLeft ->
        val tarName = if (outputName.endsWith(".tar.gz") || outputName.endsWith(".tgz"))
            outputName else "$outputName.tar.gz"
        val selected = src.files.filter { it.path in src.selectedPaths }
        when {
            src.isLocal && dst.isLocal -> withContext(Dispatchers.IO) {
                createLocalTar(selected, "${dst.path}/$tarName")
            }
            src.isLocal && !dst.isLocal -> withContext(Dispatchers.IO) {
                val tmp = File(context.cacheDir, tarName)
                createLocalTar(selected, tmp.absolutePath)
                sftpClient.upload(serverId, tmp.absolutePath, "${dst.path}/$tarName")
                tmp.delete()
            }
            else -> {
                val files = selected.joinToString(" ") { "'${it.path}'" }
                runRemoteCmd("tar czf '${dst.path}/$tarName' $files")
            }
        }
        srcIsLeft
    }

    fun deleteSelected() {
        val dp = _state.value.dualPane
        val srcIsLeft = dp.activePaneIsLeft
        val src = if (srcIsLeft) dp.leftPane else dp.rightPane
        val selected = src.files.filter { it.path in src.selectedPaths }
        if (selected.isEmpty()) return
        viewModelScope.launch {
            try {
                withContext(Dispatchers.IO) { selected.forEach { deleteFile(it, src) } }
                clearSelection(srcIsLeft)
                refreshPane(srcIsLeft)
            } catch (e: Exception) {
                updateDualPane { it.copy(operationError = e.message) }
            }
        }
    }

    // ── Pane operation helper ─────────────────────────────────────────────────

    private fun runPaneOperation(
        label: String = "",
        block: suspend (src: PaneState, dst: PaneState, srcIsLeft: Boolean) -> Boolean
    ) {
        val dp = _state.value.dualPane
        val srcIsLeft = dp.activePaneIsLeft
        val src = if (srcIsLeft) dp.leftPane else dp.rightPane
        val dst = if (srcIsLeft) dp.rightPane else dp.leftPane
        if (src.selectedPaths.isEmpty()) return

        viewModelScope.launch {
            updateDualPane { it.copy(isTransferring = true, transferProgress = 0f, operationLabel = label, operationError = null) }
            try {
                block(src, dst, srcIsLeft)
                updateDualPane { it.copy(isTransferring = false, transferProgress = null) }
                clearSelection(srcIsLeft)
                refreshPane(true)
                refreshPane(false)
            } catch (e: Exception) {
                updateDualPane { it.copy(isTransferring = false, transferProgress = null, operationError = e.message) }
            }
        }
    }

    // ── File operation primitives ─────────────────────────────────────────────

    private suspend fun copyFile(
        file: FileEntry,
        src: PaneState,
        dst: PaneState,
        onProgress: (Float) -> Unit = {}
    ) {
        when {
            src.isLocal && dst.isLocal -> withContext(Dispatchers.IO) {
                val s = File(file.path); val d = File(dst.path, file.name)
                if (s.isDirectory) copyLocalDir(s, d) else copyLocalFileWithProgress(s, d, onProgress)
            }
            src.isLocal && !dst.isLocal ->
                sftpClient.upload(serverId, file.path, "${dst.path}/${file.name}", onProgress)
            !src.isLocal && dst.isLocal ->
                sftpClient.download(serverId, file.path, "${dst.path}/${file.name}", onProgress)
            else -> { runRemoteCmd("cp -r '${file.path}' '${dst.path}/'"); onProgress(1f) }
        }
    }

    private fun copyLocalFileWithProgress(src: File, dst: File, onProgress: (Float) -> Unit) {
        val size = src.length()
        if (size == 0L) { src.copyTo(dst, overwrite = true); onProgress(1f); return }
        var transferred = 0L
        var lastPct = -1
        src.inputStream().use { inp ->
            dst.outputStream().use { out ->
                val buf = ByteArray(65536)
                while (true) {
                    val n = inp.read(buf); if (n == -1) break
                    out.write(buf, 0, n)
                    transferred += n
                    val pct = (transferred * 100 / size).toInt()
                    if (pct != lastPct) { lastPct = pct; onProgress(transferred.toFloat() / size) }
                }
            }
        }
    }

    private suspend fun deleteFile(file: FileEntry, pane: PaneState) {
        if (pane.isLocal) {
            withContext(Dispatchers.IO) { File(file.path).deleteRecursively() }
        } else if (file.isDirectory) {
            runRemoteCmd("rm -rf '${file.path}'")
        } else {
            sftpClient.delete(serverId, file.path)
        }
    }

    private fun runRemoteCmd(cmd: String) =
        sendInput(_state.value.currentSessionId, "$cmd\n".toByteArray())

    // ── File listing ──────────────────────────────────────────────────────────

    private fun listLocalFiles(path: String): List<FileEntry> = try {
        File(path).listFiles()
            ?.map { f -> FileEntry(f.name, f.absolutePath, if (f.isFile) f.length() else 0L, f.isDirectory, f.lastModified()) }
            ?.sortedWith(compareByDescending<FileEntry> { it.isDirectory }.thenBy { it.name })
            ?: emptyList()
    } catch (_: Exception) { emptyList() }

    private suspend fun listRemoteFiles(path: String): List<FileEntry> =
        sftpClient.listDir(serverId, path)
            .map { rf -> FileEntry(rf.name, rf.path, rf.size, rf.isDirectory, rf.modifiedAt) }
            .sortedWith(compareByDescending<FileEntry> { it.isDirectory }.thenBy { it.name })

    // ── Archive helpers ───────────────────────────────────────────────────────

    private fun createLocalZip(files: List<FileEntry>, outputPath: String) {
        ZipOutputStream(FileOutputStream(outputPath).buffered()).use { zos ->
            files.forEach { entry ->
                val f = File(entry.path)
                if (f.isDirectory) addDirToZip(zos, f, f.name)
                else { zos.putNextEntry(ZipEntry(f.name)); f.inputStream().use { it.copyTo(zos) }; zos.closeEntry() }
            }
        }
    }

    private fun addDirToZip(zos: ZipOutputStream, dir: File, base: String) {
        dir.listFiles()?.forEach { child ->
            val name = "$base/${child.name}"
            if (child.isDirectory) addDirToZip(zos, child, name)
            else { zos.putNextEntry(ZipEntry(name)); child.inputStream().use { it.copyTo(zos) }; zos.closeEntry() }
        }
    }

    private fun createLocalTar(files: List<FileEntry>, outputPath: String) {
        try {
            val args = mutableListOf("tar", "czf", outputPath) + files.map { it.path }
            val proc = ProcessBuilder(args).redirectErrorStream(true).start()
            proc.waitFor()
        } catch (_: Exception) {
            // fallback: create as zip with .tar.gz name
            createLocalZip(files, outputPath)
        }
    }

    private fun copyLocalDir(src: File, dst: File) {
        dst.mkdirs()
        src.listFiles()?.forEach { child ->
            if (child.isDirectory) copyLocalDir(child, File(dst, child.name))
            else child.copyTo(File(dst, child.name), overwrite = true)
        }
    }

    // ── State update helpers ──────────────────────────────────────────────────

    private fun updateDualPane(f: (DualPaneState) -> DualPaneState) =
        _state.update { it.copy(dualPane = f(it.dualPane)) }

    private fun updatePane(isLeft: Boolean, f: (PaneState) -> PaneState) = updateDualPane { dp ->
        if (isLeft) dp.copy(leftPane = f(dp.leftPane)) else dp.copy(rightPane = f(dp.rightPane))
    }

    // ── TerminalSessionClient ─────────────────────────────────────────────────

    override fun onTextChanged(session: TerminalSession) { terminalViewRef?.get()?.onScreenUpdated() }
    override fun onSessionFinished(session: TerminalSession) {}
    override fun onCopyTextToClipboard(session: TerminalSession, text: String) {}
    override fun onPasteTextFromClipboard(session: TerminalSession) {}
    override fun onBell(session: TerminalSession) {}
    override fun onColorsChanged(session: TerminalSession) {}
    override fun onTitleChanged(session: TerminalSession) {}
    override fun onTerminalCursorStateChange(state: Boolean) {}
    override fun getTerminalCursorStyle(): Int? = null
    override fun logError(tag: String, message: String) {}
    override fun logWarn(tag: String, message: String) {}
    override fun logInfo(tag: String, message: String) {}
    override fun logDebug(tag: String, message: String) {}
    override fun logVerbose(tag: String, message: String) {}
    override fun logStackTraceWithMessage(tag: String, message: String, e: Exception) {}
    override fun logStackTrace(tag: String, e: Exception) {}

    // ── TerminalViewClient ────────────────────────────────────────────────────

    override fun onScale(scale: Float): Float = scale

    override fun onSingleTapUp(e: MotionEvent) {
        terminalViewRef?.get()?.let { v ->
            v.requestFocus()
            (v.context.getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager)
                ?.showSoftInput(v, InputMethodManager.SHOW_IMPLICIT)
        }
    }

    private fun getSessionId(session: TerminalSession) =
        _state.value.sessions.find { it.terminalSession == session }?.id ?: _state.value.currentSessionId

    override fun shouldBackButtonBeMappedToEscape(): Boolean = false
    override fun shouldEnforceCharBasedInput(): Boolean = false
    override fun shouldUseCtrlSpaceWorkaround(): Boolean = false
    override fun isTerminalViewSelected(): Boolean = true
    override fun copyModeChanged(b: Boolean) {}
    override fun onKeyUp(keyCode: Int, e: KeyEvent): Boolean = false
    override fun onLongPress(event: MotionEvent): Boolean = false
    override fun readControlKey(): Boolean = false
    override fun readAltKey(): Boolean = false
    override fun readShiftKey(): Boolean = false
    override fun readFnKey(): Boolean = false
    override fun onEmulatorSet() {}

    override fun onKeyDown(keyCode: Int, e: KeyEvent, session: TerminalSession): Boolean {
        val sessionId = getSessionId(session)
        val bytes: ByteArray? = when (keyCode) {
            KeyEvent.KEYCODE_ENTER -> "\r".toByteArray()
            KeyEvent.KEYCODE_DEL -> byteArrayOf(0x7f)
            KeyEvent.KEYCODE_FORWARD_DEL -> "\u001b[3~".toByteArray()
            KeyEvent.KEYCODE_TAB -> "\t".toByteArray()
            KeyEvent.KEYCODE_ESCAPE -> "\u001b".toByteArray()
            KeyEvent.KEYCODE_DPAD_UP -> "\u001b[A".toByteArray()
            KeyEvent.KEYCODE_DPAD_DOWN -> "\u001b[B".toByteArray()
            KeyEvent.KEYCODE_DPAD_RIGHT -> "\u001b[C".toByteArray()
            KeyEvent.KEYCODE_DPAD_LEFT -> "\u001b[D".toByteArray()
            KeyEvent.KEYCODE_MOVE_HOME -> "\u001b[1~".toByteArray()
            KeyEvent.KEYCODE_MOVE_END -> "\u001b[4~".toByteArray()
            KeyEvent.KEYCODE_PAGE_UP -> "\u001b[5~".toByteArray()
            KeyEvent.KEYCODE_PAGE_DOWN -> "\u001b[6~".toByteArray()
            KeyEvent.KEYCODE_INSERT -> "\u001b[2~".toByteArray()
            else -> null
        }
        if (bytes != null) { sendInput(sessionId, bytes); return true }
        return false
    }

    override fun onCodePoint(codePoint: Int, ctrlDown: Boolean, session: TerminalSession): Boolean {
        val sessionId = getSessionId(session)
        if (ctrlDown && (codePoint in 'a'.code..'z'.code || codePoint in 'A'.code..'Z'.code)) {
            sendInput(sessionId, byteArrayOf((Character.toLowerCase(codePoint.toChar()) - 'a' + 1).toByte()))
            return true
        }
        sendInput(sessionId, StringBuilder().appendCodePoint(codePoint).toString().toByteArray())
        return true
    }

    fun sendInput(sessionId: Int, data: ByteArray) {
        viewModelScope.launch(Dispatchers.IO) {
            try { shellMap[sessionId]?.second?.let { it.write(data); it.flush() } }
            catch (_: Exception) {}
        }
    }

    override fun onCleared() {
        shellMap.values.forEach { (shell, _) -> try { shell.close() } catch (_: Exception) {} }
        shellMap.clear()
        _state.value.sessions.forEach { it.terminalSession?.finishIfRunning() }
    }
}
