package com.openjuicessh.app.core.terminal

import org.junit.Assert.*
import org.junit.Test
import java.nio.ByteBuffer
import java.nio.ByteOrder

class TerminalSnapshotTest {

    // ── Buffer builder ─────────────────────────────────────────────────────────

    /** Packs ARGB the same way TerminalSnapshot.packArgb does. */
    private fun argb(r: Int, g: Int, b: Int): Int =
        (0xFF shl 24) or (r shl 16) or (g shl 8) or b

    private data class CellSpec(
        val codepoint: Int = 0,
        val fgR: Int = 0, val fgG: Int = 0, val fgB: Int = 0,
        val bgR: Int = 0, val bgG: Int = 0, val bgB: Int = 0,
        val flags: Int = 0,
    )

    /**
     * Builds a minimal valid snapshot buffer. [cells] is a sparse map of
     * (cellIndex -> CellSpec); unspecified cells are zeroed.
     *
     * Optionally appends a grapheme-extras section at [extrasOffset]. Each
     * entry in [graphemeExtras] is (cellIndex -> list-of-extra-codepoints).
     */
    private fun buildBuffer(
        cols: Int,
        rows: Int,
        cursorX: Int = 0,
        cursorY: Int = 0,
        cursorVisible: Boolean = true,
        defaultBg: Triple<Int, Int, Int> = Triple(10, 20, 30),
        defaultFg: Triple<Int, Int, Int> = Triple(200, 210, 220),
        cells: Map<Int, CellSpec> = emptyMap(),
        graphemeExtras: Map<Int, List<Int>> = emptyMap(),
    ): ByteBuffer {
        val cellCount = cols * rows
        val headerBytes = 12 * 4
        val cellBytes  = cellCount * 11

        // Compute the extras section offset and size
        val extrasOffset: Int
        val extrasSectionBytes: Int
        if (graphemeExtras.isEmpty()) {
            extrasOffset = 0
            extrasSectionBytes = 0
        } else {
            extrasOffset = headerBytes + cellBytes
            // 4 bytes for record count + per record: 4 (cellIdx) + 4 (count) + N*4
            extrasSectionBytes = 4 + graphemeExtras.entries.sumOf { (_, cps) -> 8 + cps.size * 4 }
        }

        val totalSize = headerBytes + cellBytes + extrasSectionBytes
        val buf = ByteBuffer.allocate(totalSize).order(ByteOrder.LITTLE_ENDIAN)

        // Header
        buf.putInt(cols); buf.putInt(rows)
        buf.putInt(cursorX); buf.putInt(cursorY)
        buf.putInt(if (cursorVisible) 1 else 0)
        buf.putInt(defaultBg.first); buf.putInt(defaultBg.second); buf.putInt(defaultBg.third)
        buf.putInt(defaultFg.first); buf.putInt(defaultFg.second); buf.putInt(defaultFg.third)
        buf.putInt(extrasOffset)

        // Cells
        for (i in 0 until cellCount) {
            val c = cells[i]
            buf.putInt(c?.codepoint ?: 0)
            buf.put(c?.fgR?.toByte() ?: 0)
            buf.put(c?.fgG?.toByte() ?: 0)
            buf.put(c?.fgB?.toByte() ?: 0)
            buf.put(c?.bgR?.toByte() ?: 0)
            buf.put(c?.bgG?.toByte() ?: 0)
            buf.put(c?.bgB?.toByte() ?: 0)
            buf.put(c?.flags?.toByte() ?: 0)
        }

        // Grapheme extras section
        if (graphemeExtras.isNotEmpty()) {
            buf.putInt(graphemeExtras.size)
            for ((cellIdx, cps) in graphemeExtras) {
                buf.putInt(cellIdx)
                buf.putInt(cps.size)
                cps.forEach { buf.putInt(it) }
            }
        }

        return buf
    }

    // ── Header / dimensions ────────────────────────────────────────────────────

    @Test
    fun `parses cols and rows from header`() {
        val snap = TerminalSnapshot.fromByteBuffer(buildBuffer(cols = 80, rows = 24))
        assertEquals(80, snap.cols)
        assertEquals(24, snap.rows)
    }

    @Test
    fun `parses cursor position and visibility`() {
        val snap = TerminalSnapshot.fromByteBuffer(
            buildBuffer(cols = 10, rows = 5, cursorX = 7, cursorY = 3, cursorVisible = true)
        )
        assertEquals(7, snap.cursorX)
        assertEquals(3, snap.cursorY)
        assertTrue(snap.cursorVisible)
    }

    @Test
    fun `cursor invisible when header value is 0`() {
        val snap = TerminalSnapshot.fromByteBuffer(
            buildBuffer(cols = 10, rows = 5, cursorVisible = false)
        )
        assertFalse(snap.cursorVisible)
    }

    @Test
    fun `parses default background color as packed ARGB`() {
        val snap = TerminalSnapshot.fromByteBuffer(
            buildBuffer(cols = 2, rows = 1, defaultBg = Triple(50, 100, 150))
        )
        assertEquals(argb(50, 100, 150), snap.defaultBgArgb)
    }

    @Test
    fun `parses default foreground color as packed ARGB`() {
        val snap = TerminalSnapshot.fromByteBuffer(
            buildBuffer(cols = 2, rows = 1, defaultFg = Triple(255, 128, 0))
        )
        assertEquals(argb(255, 128, 0), snap.defaultFgArgb)
    }

    // ── Cell data ──────────────────────────────────────────────────────────────

    @Test
    fun `all cells are zero for empty grid`() {
        val snap = TerminalSnapshot.fromByteBuffer(buildBuffer(cols = 4, rows = 3))
        assertEquals(12, snap.codepoints.size)
        assertTrue(snap.codepoints.all { it == 0 })
    }

    @Test
    fun `parses ASCII codepoint in specified cell`() {
        val snap = TerminalSnapshot.fromByteBuffer(
            buildBuffer(cols = 3, rows = 1, cells = mapOf(1 to CellSpec(codepoint = 'B'.code)))
        )
        assertEquals('B'.code, snap.codepoints[1])
    }

    @Test
    fun `parses Unicode codepoint beyond BMP`() {
        val codepoint = 0x1F600  // 😀 emoji
        val snap = TerminalSnapshot.fromByteBuffer(
            buildBuffer(cols = 2, rows = 1, cells = mapOf(0 to CellSpec(codepoint = codepoint)))
        )
        assertEquals(codepoint, snap.codepoints[0])
    }

    @Test
    fun `parses foreground color per cell`() {
        val snap = TerminalSnapshot.fromByteBuffer(
            buildBuffer(
                cols = 2, rows = 1,
                cells = mapOf(0 to CellSpec(fgR = 255, fgG = 0, fgB = 128))
            )
        )
        assertEquals(argb(255, 0, 128), snap.fgArgb[0])
    }

    @Test
    fun `parses background color per cell`() {
        val snap = TerminalSnapshot.fromByteBuffer(
            buildBuffer(
                cols = 2, rows = 1,
                cells = mapOf(1 to CellSpec(bgR = 0, bgG = 64, bgB = 255))
            )
        )
        assertEquals(argb(0, 64, 255), snap.bgArgb[1])
    }

    @Test
    fun `parses cell flag byte`() {
        val snap = TerminalSnapshot.fromByteBuffer(
            buildBuffer(
                cols = 3, rows = 1,
                cells = mapOf(
                    0 to CellSpec(codepoint = '界'.code, flags = 0),                // wide char base
                    1 to CellSpec(flags = TerminalSnapshot.CELL_FLAG_SPACER),      // spacer
                    2 to CellSpec(flags = TerminalSnapshot.CELL_FLAG_HAS_GRAPHEME) // grapheme
                )
            )
        )
        assertEquals(0, snap.flags[0].toInt() and 0xFF)
        assertEquals(TerminalSnapshot.CELL_FLAG_SPACER,     snap.flags[1].toInt() and 0xFF)
        assertEquals(TerminalSnapshot.CELL_FLAG_HAS_GRAPHEME, snap.flags[2].toInt() and 0xFF)
    }

    @Test
    fun `multiple cells across rows are indexed correctly`() {
        // 3 cols × 2 rows — cell at (col=2, row=1) is index 5
        val snap = TerminalSnapshot.fromByteBuffer(
            buildBuffer(cols = 3, rows = 2, cells = mapOf(5 to CellSpec(codepoint = 'Z'.code)))
        )
        assertEquals('Z'.code, snap.codepoints[5])
        assertEquals(0, snap.codepoints[0])
    }

    // ── Grapheme extras ────────────────────────────────────────────────────────

    @Test
    fun `grapheme extras are empty when extrasOffset is zero`() {
        val snap = TerminalSnapshot.fromByteBuffer(buildBuffer(cols = 2, rows = 1))
        assertTrue(snap.graphemeExtras.isEmpty())
    }

    @Test
    fun `parses grapheme extras section`() {
        val snap = TerminalSnapshot.fromByteBuffer(
            buildBuffer(
                cols = 2, rows = 1,
                cells = mapOf(0 to CellSpec(
                    codepoint = 'a'.code,
                    flags = TerminalSnapshot.CELL_FLAG_HAS_GRAPHEME
                )),
                graphemeExtras = mapOf(0 to listOf(0x0301)) // combining acute accent
            )
        )
        assertNotNull(snap.graphemeExtras[0])
        assertEquals(1, snap.graphemeExtras[0]!!.size)
        assertEquals(0x0301, snap.graphemeExtras[0]!![0])
    }

    @Test
    fun `parses multiple grapheme extras entries`() {
        val snap = TerminalSnapshot.fromByteBuffer(
            buildBuffer(
                cols = 4, rows = 1,
                graphemeExtras = mapOf(
                    0 to listOf(0x0301, 0x0302),
                    3 to listOf(0x20DD)
                )
            )
        )
        assertEquals(listOf(0x0301, 0x0302), snap.graphemeExtras[0]!!.toList())
        assertEquals(listOf(0x20DD),         snap.graphemeExtras[3]!!.toList())
    }

    // ── Error handling ─────────────────────────────────────────────────────────

    @Test(expected = IllegalArgumentException::class)
    fun `throws when buffer too small for declared grid`() {
        // Declare 10×10 grid but only allocate enough for 2×2
        val smallBuf = buildBuffer(cols = 2, rows = 2)
        // Manually patch cols to 10, rows to 10 in the buffer without resizing
        val patchedBuf = ByteBuffer.allocate(smallBuf.capacity()).order(ByteOrder.LITTLE_ENDIAN)
        patchedBuf.putInt(10)  // cols = 10
        patchedBuf.putInt(10)  // rows = 10
        // fill rest with zeros
        while (patchedBuf.hasRemaining()) patchedBuf.put(0)
        TerminalSnapshot.fromByteBuffer(patchedBuf)
    }

    // ── Equality / hashCode ────────────────────────────────────────────────────

    @Test
    fun `equal snapshots with same content are equal`() {
        val buf1 = buildBuffer(cols = 2, rows = 1, cells = mapOf(0 to CellSpec(codepoint = 'X'.code)))
        val buf2 = buildBuffer(cols = 2, rows = 1, cells = mapOf(0 to CellSpec(codepoint = 'X'.code)))
        assertEquals(TerminalSnapshot.fromByteBuffer(buf1), TerminalSnapshot.fromByteBuffer(buf2))
    }

    @Test
    fun `snapshots differ when codepoint changes`() {
        val buf1 = buildBuffer(cols = 2, rows = 1, cells = mapOf(0 to CellSpec(codepoint = 'A'.code)))
        val buf2 = buildBuffer(cols = 2, rows = 1, cells = mapOf(0 to CellSpec(codepoint = 'B'.code)))
        assertNotEquals(TerminalSnapshot.fromByteBuffer(buf1), TerminalSnapshot.fromByteBuffer(buf2))
    }

    @Test
    fun `snapshots differ when cursor position changes`() {
        val buf1 = buildBuffer(cols = 5, rows = 1, cursorX = 0)
        val buf2 = buildBuffer(cols = 5, rows = 1, cursorX = 4)
        assertNotEquals(TerminalSnapshot.fromByteBuffer(buf1), TerminalSnapshot.fromByteBuffer(buf2))
    }
}
