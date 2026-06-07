package com.openjuicessh.app.core.terminal

/**
 * Pure function that converts an EditText content change into terminal bytes.
 *
 * Returns a pair of (bytesToSend, newSentLength).
 *
 * Rules:
 *  - Added characters → UTF-8 bytes, with \n replaced by \r (soft-keyboard Enter)
 *  - Deleted characters → one DEL byte (0x7F) per removed character
 *  - No change → empty byte array
 */
object TerminalInputEncoder {
    fun encode(currentText: String, sentLength: Int): Pair<ByteArray, Int> = when {
        currentText.length > sentLength -> {
            val added = currentText.substring(sentLength).replace("\n", "\r")
            Pair(added.toByteArray(Charsets.UTF_8), currentText.length)
        }
        currentText.length < sentLength -> {
            val count = sentLength - currentText.length
            Pair(ByteArray(count) { 0x7F.toByte() }, currentText.length)
        }
        else -> Pair(ByteArray(0), sentLength)
    }
}
