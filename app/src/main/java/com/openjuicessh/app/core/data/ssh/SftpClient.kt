package com.openjuicessh.app.core.data.ssh

import com.openjuicessh.app.core.domain.model.RemoteFile
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import net.schmizz.sshj.common.StreamCopier
import net.schmizz.sshj.sftp.SFTPClient
import net.schmizz.sshj.sftp.FileAttributes
import net.schmizz.sshj.sftp.RemoteResourceInfo
import net.schmizz.sshj.xfer.FileSystemFile
import net.schmizz.sshj.xfer.TransferListener
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SftpClient @Inject constructor(
    private val sessionManager: SshSessionManager
) {
    private fun getSftpClient(serverId: Long): SFTPClient {
        val ssh = sessionManager.getSession(serverId)
            ?: throw IllegalStateException("Not connected to server")
        return ssh.newSFTPClient()
    }

    suspend fun listDir(serverId: Long, path: String): List<RemoteFile> = withContext(Dispatchers.IO) {
        val sftp = getSftpClient(serverId)
        try {
            sftp.ls(path).map { it.toRemoteFile(path) }
        } finally {
            sftp.close()
        }
    }

    suspend fun download(
        serverId: Long,
        remotePath: String,
        localPath: String,
        onProgress: (Float) -> Unit = {}
    ) = withContext(Dispatchers.IO) {
        val sftp = getSftpClient(serverId)
        try {
            sftp.fileTransfer.transferListener = progressTransferListener(onProgress)
            sftp.get(remotePath, FileSystemFile(localPath))
        } finally {
            sftp.close()
        }
    }

    suspend fun upload(
        serverId: Long,
        localPath: String,
        remotePath: String,
        onProgress: (Float) -> Unit = {}
    ) = withContext(Dispatchers.IO) {
        val sftp = getSftpClient(serverId)
        try {
            sftp.fileTransfer.transferListener = progressTransferListener(onProgress)
            sftp.put(FileSystemFile(localPath), remotePath)
        } finally {
            sftp.close()
        }
    }

    private fun progressTransferListener(onProgress: (Float) -> Unit) = object : TransferListener {
        override fun directory(name: String): TransferListener = this
        override fun file(name: String, size: Long): StreamCopier.Listener {
            var lastPct = -1
            return StreamCopier.Listener { transferred ->
                if (size > 0) {
                    val pct = (transferred * 100 / size).toInt()
                    if (pct != lastPct) { lastPct = pct; onProgress(transferred.toFloat() / size) }
                }
            }
        }
    }

    suspend fun delete(serverId: Long, path: String) = withContext(Dispatchers.IO) {
        val sftp = getSftpClient(serverId)
        try {
            sftp.rm(path)
        } finally {
            sftp.close()
        }
    }

    suspend fun rename(serverId: Long, oldPath: String, newPath: String) = withContext(Dispatchers.IO) {
        val sftp = getSftpClient(serverId)
        try {
            sftp.rename(oldPath, newPath)
        } finally {
            sftp.close()
        }
    }

    suspend fun mkdir(serverId: Long, path: String) = withContext(Dispatchers.IO) {
        val sftp = getSftpClient(serverId)
        try {
            sftp.mkdir(path)
        } finally {
            sftp.close()
        }
    }

    suspend fun chmod(serverId: Long, path: String, permissions: Int) = withContext(Dispatchers.IO) {
        val sftp = getSftpClient(serverId)
        try {
            sftp.chmod(path, permissions)
        } finally {
            sftp.close()
        }
    }

    private fun RemoteResourceInfo.toRemoteFile(parentPath: String): RemoteFile {
        val attrs = attributes
        return RemoteFile(
            path = if (parentPath.endsWith("/")) "$parentPath$name" else "$parentPath/$name",
            name = name,
            size = attrs.size,
            permissions = attrs.mode.toString(),
            modifiedAt = attrs.mtime * 1000L,
            isDirectory = isDirectory,
            owner = attrs.uid.toString(),
            group = attrs.gid.toString()
        )
    }
}
