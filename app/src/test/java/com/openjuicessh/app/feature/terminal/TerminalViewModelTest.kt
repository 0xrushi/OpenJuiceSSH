package com.openjuicessh.app.feature.terminal

import android.content.Context
import app.cash.turbine.test
import com.openjuicessh.app.core.data.ssh.SftpClient
import com.openjuicessh.app.core.data.ssh.SshSessionManager
import com.openjuicessh.app.core.data.ssh.TerminalSessionManager
import com.openjuicessh.app.core.domain.model.AuthType
import com.openjuicessh.app.core.domain.model.Server
import com.openjuicessh.app.core.domain.model.Snippet
import com.openjuicessh.app.core.domain.repository.ServerRepository
import com.openjuicessh.app.core.domain.repository.SnippetRepository
import io.mockk.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.*
import androidx.lifecycle.SavedStateHandle
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class TerminalViewModelTest {

    private val testDispatcher = UnconfinedTestDispatcher()

    private val mockSshManager       = mockk<SshSessionManager>(relaxed = true)
    private val mockTerminalManager  = mockk<TerminalSessionManager>(relaxed = true)
    private val mockServerRepo       = mockk<ServerRepository>()
    private val mockSnippetRepo      = mockk<SnippetRepository>()
    private val mockSftpClient       = mockk<SftpClient>(relaxed = true)
    private val mockContext          = mockk<Context>(relaxed = true)

    private val serverId = 42L

    private lateinit var viewModel: TerminalViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)

        // Minimal stubs so the init block doesn't crash
        every { mockTerminalManager.sessions } returns MutableStateFlow(emptyMap())
        every { mockSnippetRepo.getAllSnippets() } returns flowOf(emptyList())
        // Default: no server found → error state set, no connection attempted
        coEvery { mockServerRepo.getServerById(serverId) } returns null
    }

    private fun buildViewModel(): TerminalViewModel = TerminalViewModel(
        sessionManager       = mockSshManager,
        terminalSessionManager = mockTerminalManager,
        serverRepository     = mockServerRepo,
        snippetRepository    = mockSnippetRepo,
        sftpClient           = mockSftpClient,
        savedStateHandle     = SavedStateHandle(mapOf("serverId" to serverId)),
        context              = mockContext,
    )

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    // ── selectAll ─────────────────────────────────────────────────────────────

    @Test
    fun `selectAll returns text from manager`() {
        every { mockTerminalManager.selectAll(serverId, 0) } returns "terminal output"
        viewModel = buildViewModel()

        assertEquals("terminal output", viewModel.selectAll(0))
    }

    @Test
    fun `selectAll returns null when manager returns null`() {
        every { mockTerminalManager.selectAll(serverId, any()) } returns null
        viewModel = buildViewModel()

        assertNull(viewModel.selectAll(0))
    }

    // ── formatSelectionRange ──────────────────────────────────────────────────

    @Test
    fun `formatSelectionRange passes correct args to manager`() {
        every { mockTerminalManager.formatSelectionRange(serverId, 0, 5, 20) } returns "selected text"
        viewModel = buildViewModel()

        assertEquals("selected text", viewModel.formatSelectionRange(0, 5, 20))
        verify { mockTerminalManager.formatSelectionRange(serverId, 0, 5, 20) }
    }

    @Test
    fun `formatSelectionRange returns null on manager null`() {
        every { mockTerminalManager.formatSelectionRange(any(), any(), any(), any()) } returns null
        viewModel = buildViewModel()

        assertNull(viewModel.formatSelectionRange(0, 0, 10))
    }

    // ── resize ────────────────────────────────────────────────────────────────

    @Test
    fun `resize delegates to manager with serverId`() {
        viewModel = buildViewModel()
        viewModel.resize(sessionId = 1, cols = 80, rows = 24, cellW = 9, cellH = 18)

        verify { mockTerminalManager.resize(serverId, 1, 80, 24, 9, 18) }
    }

    // ── sendInput ─────────────────────────────────────────────────────────────

    @Test
    fun `sendInput delegates bytes to manager`() {
        viewModel = buildViewModel()
        val data = "ls\r".toByteArray()
        viewModel.sendInput(sessionId = 0, data = data)

        verify { mockTerminalManager.sendInput(serverId, 0, data) }
    }

    // ── error state ───────────────────────────────────────────────────────────

    @Test
    fun `state shows error when server not found`() = runTest {
        coEvery { mockServerRepo.getServerById(serverId) } returns null
        viewModel = buildViewModel()

        viewModel.state.test {
            val state = awaitItem()
            assertNotNull(state.error)
            assertTrue(state.error!!.contains("not found", ignoreCase = true))
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `state shows server name after successful load`() {
        val server = Server(
            id = serverId, name = "prod-box",
            host = "10.0.0.1", port = 22,
            username = "admin",
            authType = AuthType.PASSWORD,
            credentialRef = "cred-1",
        )
        coEvery { mockServerRepo.getServerById(serverId) } returns server
        every { mockSshManager.isConnected(serverId) } returns true

        viewModel = buildViewModel()

        // UnconfinedTestDispatcher runs the init coroutine eagerly on this thread,
        // so serverName is set synchronously by the time buildViewModel() returns.
        assertEquals("prod-box", viewModel.state.value.serverName)
        assertNull(viewModel.state.value.error)
    }

    // ── panel switching ───────────────────────────────────────────────────────

    @Test
    fun `setActivePanel updates state panel`() = runTest {
        viewModel = buildViewModel()

        viewModel.setActivePanel(TerminalPanel.SFTP)

        assertEquals(TerminalPanel.SFTP, viewModel.state.value.activePanel)
    }

    @Test
    fun `switchSession updates currentSessionId`() = runTest {
        viewModel = buildViewModel()

        viewModel.switchSession(7)

        assertEquals(7, viewModel.state.value.currentSessionId)
    }

    // ── snippets ─────────────────────────────────────────────────────────────

    @Test
    fun `snippets are loaded from repository`() = runTest {
        val snippets = listOf(
            Snippet(id = 1L, name = "ping", command = "ping 8.8.8.8"),
            Snippet(id = 2L, name = "df",   command = "df -h"),
        )
        every { mockSnippetRepo.getAllSnippets() } returns flowOf(snippets)
        viewModel = buildViewModel()

        assertEquals(2, viewModel.state.value.snippets.size)
        assertEquals("ping", viewModel.state.value.snippets[0].name)
    }
}
