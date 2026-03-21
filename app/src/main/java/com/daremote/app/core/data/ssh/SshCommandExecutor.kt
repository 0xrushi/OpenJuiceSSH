package com.daremote.app.core.data.ssh

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext
import net.schmizz.sshj.SSHClient
import net.schmizz.sshj.connection.channel.direct.Session
import java.io.BufferedReader
import java.io.InputStreamReader
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SshCommandExecutor @Inject constructor(
    private val sessionManager: SshSessionManager
) {
    suspend fun execute(serverId: Long, command: String): Result<String> = withContext(Dispatchers.IO) {
        try {
            val client = sessionManager.getSession(serverId)
                ?: return@withContext Result.failure(IllegalStateException("Not connected"))

            val session = client.startSession()
            try {
                val cmd = session.exec(command)
                val output = BufferedReader(InputStreamReader(cmd.inputStream)).readText()
                val error = BufferedReader(InputStreamReader(cmd.errorStream)).readText()
                cmd.join()

                if (cmd.exitStatus == 0 || error.isBlank()) {
                    Result.success(output)
                } else {
                    Result.failure(RuntimeException("Exit ${cmd.exitStatus}: $error"))
                }
            } finally {
                session.close()
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun executeStreaming(serverId: Long, command: String): Flow<String> = flow {
        val client = sessionManager.getSession(serverId)
            ?: throw IllegalStateException("Not connected")

        val session = client.startSession()
        try {
            val cmd = session.exec(command)
            val reader = BufferedReader(InputStreamReader(cmd.inputStream))
            var line = reader.readLine()
            while (line != null) {
                emit(line)
                line = reader.readLine()
            }
            cmd.join()
        } finally {
            session.close()
        }
    }.flowOn(Dispatchers.IO)
}
