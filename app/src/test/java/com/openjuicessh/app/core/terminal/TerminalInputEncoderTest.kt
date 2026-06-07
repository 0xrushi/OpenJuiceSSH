package com.openjuicessh.app.core.terminal

import org.junit.Assert.*
import org.junit.Test

class TerminalInputEncoderTest {

    private fun encode(text: String, sentLength: Int) = TerminalInputEncoder.encode(text, sentLength)

    // ── No change ─────────────────────────────────────────────────────────────

    @Test
    fun `no change when text equals sentLength`() {
        val (bytes, newLen) = encode("hello", sentLength = 5)
        assertEquals(0, bytes.size)
        assertEquals(5, newLen)
    }

    @Test
    fun `empty text with zero sentLength produces nothing`() {
        val (bytes, newLen) = encode("", sentLength = 0)
        assertEquals(0, bytes.size)
        assertEquals(0, newLen)
    }

    // ── New characters ─────────────────────────────────────────────────────────

    @Test
    fun `single ASCII character is forwarded as its byte`() {
        val (bytes, newLen) = encode("a", sentLength = 0)
        assertArrayEquals(byteArrayOf('a'.code.toByte()), bytes)
        assertEquals(1, newLen)
    }

    @Test
    fun `only the newly added suffix is forwarded`() {
        val (bytes, newLen) = encode("hello", sentLength = 3)
        assertArrayEquals("lo".toByteArray(Charsets.UTF_8), bytes)
        assertEquals(5, newLen)
    }

    @Test
    fun `newline is replaced by carriage return`() {
        val (bytes, newLen) = encode("\n", sentLength = 0)
        assertArrayEquals(byteArrayOf(0x0D), bytes)
        assertEquals(1, newLen)
    }

    @Test
    fun `newline embedded in word is replaced by carriage return`() {
        val (bytes, newLen) = encode("ls\n", sentLength = 0)
        val expected = "ls\r".toByteArray(Charsets.UTF_8)
        assertArrayEquals(expected, bytes)
        assertEquals(3, newLen)
    }

    @Test
    fun `multiple newlines are all replaced`() {
        val (bytes, _) = encode("\n\n\n", sentLength = 0)
        assertArrayEquals(byteArrayOf(0x0D, 0x0D, 0x0D), bytes)
    }

    @Test
    fun `unicode multibyte character produces correct UTF-8 bytes`() {
        val (bytes, newLen) = encode("é", sentLength = 0) // U+00E9, UTF-8: 0xC3 0xA9
        assertArrayEquals(byteArrayOf(0xC3.toByte(), 0xA9.toByte()), bytes)
        assertEquals(1, newLen) // String length is 1 char
    }

    @Test
    fun `surrogate pair emoji produces correct UTF-8 bytes`() {
        val emoji = "😀"  // 😀 U+1F600
        val (bytes, newLen) = encode(emoji, sentLength = 0)
        // UTF-8 encoding of U+1F600: F0 9F 98 80
        assertArrayEquals(
            byteArrayOf(0xF0.toByte(), 0x9F.toByte(), 0x98.toByte(), 0x80.toByte()),
            bytes
        )
        assertEquals(2, newLen) // surrogate pair = 2 chars in String
    }

    @Test
    fun `partial send then extend sends only delta`() {
        // First event: "git " sent
        val (bytes1, len1) = encode("git ", sentLength = 0)
        assertArrayEquals("git ".toByteArray(), bytes1)

        // Second event: " status" appended
        val (bytes2, len2) = encode("git status", sentLength = len1)
        assertArrayEquals("status".toByteArray(), bytes2)
        assertEquals(10, len2)
    }

    // ── Backspace / deletion ──────────────────────────────────────────────────

    @Test
    fun `single deletion sends one DEL byte`() {
        val (bytes, newLen) = encode("hel", sentLength = 4) // "hell" → "hel"
        assertArrayEquals(byteArrayOf(0x7F), bytes)
        assertEquals(3, newLen)
    }

    @Test
    fun `deleting three characters sends three DEL bytes`() {
        val (bytes, newLen) = encode("", sentLength = 3)
        assertArrayEquals(byteArrayOf(0x7F, 0x7F, 0x7F), bytes)
        assertEquals(0, newLen)
    }

    @Test
    fun `DEL bytes are 0x7F not 0x08`() {
        val (bytes, _) = encode("a", sentLength = 2)
        assertEquals(0x7F.toByte(), bytes[0])
    }

    // ── Edge cases ────────────────────────────────────────────────────────────

    @Test
    fun `large string is forwarded completely`() {
        val big = "a".repeat(10_000)
        val (bytes, newLen) = encode(big, sentLength = 0)
        assertEquals(10_000, bytes.size)
        assertEquals(10_000, newLen)
        assertTrue(bytes.all { it == 'a'.code.toByte() })
    }

    @Test
    fun `sentLength advances correctly across multiple edits`() {
        var sent = 0
        val allSent = mutableListOf<Byte>()

        listOf("h", "he", "hel", "hell", "hello").forEach { text ->
            val (bytes, newLen) = encode(text, sent)
            allSent += bytes.toList()
            sent = newLen
        }

        assertEquals("hello".toByteArray().toList(), allSent)
    }
}
