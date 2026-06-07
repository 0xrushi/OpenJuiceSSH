package com.openjuicessh.app.core.data.ssh

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.concurrent.atomic.AtomicInteger

/**
 * Uses StandardTestDispatcher (the runTest default) with explicit runCurrent() calls.
 *
 * Why runCurrent() is required after each onDataChunkReceived():
 *
 * With StandardTestDispatcher, scope.launch{} queues the coroutine but does not run it.
 * advanceTimeBy(N) advances the clock to currentTime+N *before* running queued tasks, so
 * a delay(30) called inside a just-launched coroutine would be measured from the already-
 * advanced clock (e.g. delay(30) at t=29 → fires at t=59, not t=30).
 *
 * Calling runCurrent() immediately after onDataChunkReceived() runs the queued coroutine
 * at the *current* virtual time (still t=0 or whatever the test has advanced to), so
 * delay(finalDelayMs) is anchored correctly relative to the chunk's arrival time.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class BurstSnapshotSchedulerTest {

    private fun TestScope.makeScheduler(
        count: AtomicInteger,
        throttleMs: Long = 16L,
        finalDelayMs: Long = 30L,
    ) = BurstSnapshotScheduler(
        scope        = this,
        throttleMs   = throttleMs,
        finalDelayMs = finalDelayMs,
        timeMs       = { testScheduler.currentTime },
        onSnapshot   = { count.incrementAndGet() },
    )

    /** Deliver one data chunk and start any newly-launched final-snapshot job. */
    private suspend fun TestScope.chunk(s: BurstSnapshotScheduler) {
        s.onDataChunkReceived()
        runCurrent()
    }

    // ── Immediate snapshot ─────────────────────────────────────────────────────

    @Test
    fun `first chunk fires immediately`() = runTest {
        val count = AtomicInteger(0)
        chunk(makeScheduler(count))
        assertEquals(1, count.get())
    }

    @Test
    fun `chunk after throttle window also fires immediately`() = runTest {
        val count = AtomicInteger(0)
        val s = makeScheduler(count)

        chunk(s)              // t=0  → immediate → 1
        advanceTimeBy(20)     // t=20 (> 16 ms)
        chunk(s)              // t=20 → immediate → 2

        assertEquals(2, count.get())
    }

    // ── Throttle ───────────────────────────────────────────────────────────────

    @Test
    fun `chunk within throttle window is suppressed`() = runTest {
        val count = AtomicInteger(0)
        val s = makeScheduler(count)

        chunk(s)              // t=0 → immediate → 1
        advanceTimeBy(5)      // t=5  (< 16 ms)
        chunk(s)              // throttled

        assertEquals(1, count.get())
    }

    @Test
    fun `burst of chunks produces one immediate snapshot per 16ms window`() = runTest {
        val count = AtomicInteger(0)
        val s = makeScheduler(count)

        // 9 chunks × 5 ms apart → immediate at t=0, 20, 40 → 3 immediate snapshots
        repeat(9) {
            chunk(s)
            advanceTimeBy(5)
        }
        // After the last chunk (t=45), the final snapshot fires at t=45+30=75.
        advanceTimeBy(35)     // → t=80; final fires

        val total = count.get()
        assertTrue("expected 3–5 snapshots (3 immediate + 1 final), got $total", total in 3..5)
    }

    // ── Final snapshot ─────────────────────────────────────────────────────────

    @Test
    fun `final snapshot fires 30ms after last chunk when no more data arrives`() = runTest {
        val count = AtomicInteger(0)
        val s = makeScheduler(count)

        chunk(s)              // t=0  → immediate → 1; final at t=30
        advanceTimeBy(5)      // t=5
        chunk(s)              // throttled; final rescheduled to t=35
        advanceTimeBy(5)      // t=10
        chunk(s)              // throttled; final rescheduled to t=40

        assertEquals(1, count.get()) // no final has fired yet

        advanceTimeBy(35)     // t=45 (≥ 40) → final fires

        assertEquals(2, count.get())
    }

    @Test
    fun `single chunk still emits a final snapshot after idle period`() = runTest {
        val count = AtomicInteger(0)
        val s = makeScheduler(count)

        chunk(s)              // t=0 → immediate → 1; final at t=30

        advanceTimeBy(100)    // well past finalDelayMs

        assertEquals(2, count.get()) // immediate + final
    }

    @Test
    fun `final snapshot does not fire before finalDelayMs`() = runTest {
        val count = AtomicInteger(0)
        val s = makeScheduler(count, finalDelayMs = 30)

        chunk(s)              // t=0 → immediate → 1; final anchored at t=30

        advanceTimeBy(29)     // t=29 — final has not fired yet
        assertEquals(1, count.get())

        advanceTimeBy(2)      // t=31 — safely past the deadline
        assertEquals(2, count.get())
    }

    // ── Cancellation / rescheduling ────────────────────────────────────────────

    @Test
    fun `new chunk within window cancels and resets the pending final snapshot`() = runTest {
        val count = AtomicInteger(0)
        val s = makeScheduler(count)

        // Keep all three chunks within one 16 ms window (lastSnap=0, so window is 0..15).
        chunk(s)              // t=0  → immediate → 1; final at t=30
        advanceTimeBy(5)      // t=5
        chunk(s)              // throttled; cancel t=30, final at t=35
        advanceTimeBy(5)      // t=10
        chunk(s)              // throttled; cancel t=35, final at t=40

        advanceTimeBy(25)     // t=35 — old final (t=35) was cancelled; t=40 still pending
        assertEquals(1, count.get())

        advanceTimeBy(10)     // t=45 — new final (t=40) fires
        assertEquals(2, count.get())
    }

    @Test
    fun `immediate snapshot cancels existing final so it does not double fire`() = runTest {
        val count = AtomicInteger(0)
        val s = makeScheduler(count)

        chunk(s)              // t=0  → immediate → 1; final at t=30
        advanceTimeBy(5)      // t=5
        chunk(s)              // throttled; cancel t=30, final at t=35
        advanceTimeBy(15)     // t=20
        chunk(s)              // t=20 (20−0 ≥ 16) → immediate → 2; cancel t=35, final at t=50

        advanceTimeBy(10)     // t=30 — old final gone; count still 2
        assertEquals(2, count.get())

        advanceTimeBy(25)     // t=55 — new final (t=50) fires → 3
        assertEquals(3, count.get())
    }

    // ── Edge cases ─────────────────────────────────────────────────────────────

    @Test
    fun `custom throttle and final delay are respected`() = runTest {
        val count = AtomicInteger(0)
        val s = makeScheduler(count, throttleMs = 50L, finalDelayMs = 100L)

        chunk(s)              // t=0  → immediate (0−(−50)=50 ≥ 50) → 1; final at t=100
        advanceTimeBy(30)     // t=30 (30−0=30 < 50 ms)
        chunk(s)              // throttled; cancel t=100, final at t=130
        advanceTimeBy(30)     // t=60
        chunk(s)              // t=60 (60−0=60 ≥ 50) → immediate → 2; cancel t=130, final at t=160

        assertEquals(2, count.get())

        advanceTimeBy(105)    // t=165 — final (t=160) fires → 3
        assertEquals(3, count.get())
    }

    @Test
    fun `two calls at same virtual time second is throttled`() = runTest {
        val count = AtomicInteger(0)
        val s = makeScheduler(count)

        s.onDataChunkReceived()   // t=0 → immediate → 1
        runCurrent()
        s.onDataChunkReceived()   // t=0, 0−0 = 0 < 16 → throttled
        runCurrent()

        assertEquals(1, count.get())
    }
}
