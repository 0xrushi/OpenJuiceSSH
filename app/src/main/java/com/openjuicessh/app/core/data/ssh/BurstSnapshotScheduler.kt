package com.openjuicessh.app.core.data.ssh

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * Throttles snapshot emissions during high-throughput data bursts while guaranteeing
 * a final snapshot 30 ms after the last chunk.
 *
 * Without this, the 16 ms throttle silently drops the last chunk of a burst (e.g. the
 * shell prompt line that arrives just after the previous snapshot), leaving the terminal
 * frozen with the cursor one row above the true prompt position until the user types.
 */
internal class BurstSnapshotScheduler(
    private val scope: CoroutineScope,
    private val throttleMs: Long = 16L,
    private val finalDelayMs: Long = 30L,
    private val timeMs: () -> Long = System::currentTimeMillis,
    private val onSnapshot: suspend () -> Unit,
) {
    private var lastSnapshotTime = -throttleMs   // ensures first chunk always fires immediately
    private var finalSnapshotJob: Job? = null

    suspend fun onDataChunkReceived() {
        val now = timeMs()
        if (now - lastSnapshotTime >= throttleMs) {
            finalSnapshotJob?.cancel()
            onSnapshot()
            lastSnapshotTime = now
        }
        // Always reschedule the deferred final snapshot so it fires after the burst settles.
        finalSnapshotJob?.cancel()
        finalSnapshotJob = scope.launch {
            delay(finalDelayMs)
            onSnapshot()
        }
    }
}
