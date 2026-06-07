package com.openjuicessh.app.core.terminal

import android.util.Log
import java.nio.ByteBuffer

class GhosttyBridge {
    companion object {
        private const val TAG = "GhosttyBridge"
        private val loadError: Throwable? = try {
            Log.i(TAG, "Loading libchuchu_jni...")
            System.loadLibrary("chuchu_jni")
            Log.i(TAG, "libchuchu_jni loaded OK")
            null
        } catch (e: UnsatisfiedLinkError) {
            Log.e(TAG, "UnsatisfiedLinkError loading libchuchu_jni: ${e.message}")
            e
        } catch (e: Exception) {
            Log.e(TAG, "Exception loading libchuchu_jni: ${e.message}")
            e
        }
    }

    fun nativeStatus(): String {
        val err = loadError ?: return "loaded"
        val message = err.message?.takeIf { it.isNotBlank() } ?: "unknown"
        return "not loaded (${err::class.simpleName}: $message)"
    }

    fun isLoaded(): Boolean = loadError == null

    external fun nativeVersion(): String
    external fun nativeCreate(cols: Int, rows: Int, maxScrollback: Int): Long
    external fun nativeDestroy(handle: Long)
    external fun nativeWriteRemote(handle: Long, data: ByteArray)
    external fun nativeResize(handle: Long, cols: Int, rows: Int, cellW: Int, cellH: Int)
    external fun nativeScroll(handle: Long, delta: Int, x: Float, y: Float)
    external fun nativeScrollToActive(handle: Long)
    external fun nativeSnapshot(handle: Long): ByteBuffer
    external fun nativePollTitle(handle: Long): String?
    external fun nativePollPwd(handle: Long): String?
    external fun nativeDrainBellCount(handle: Long): Int
    external fun nativeSetColorScheme(handle: Long, scheme: Int)
    external fun nativeSetDefaultColors(
        handle: Long,
        fgRgb: IntArray?,
        bgRgb: IntArray?,
        cursorRgb: IntArray?,
        paletteRgb: ByteArray?,
    )
    external fun nativeEncodeKey(handle: Long, key: Int, cp: Int, mods: Int, action: Int, utf8: String?): ByteArray?
    external fun nativeSetMouseEncodingSize(
        handle: Long,
        screenWidth: Int,
        screenHeight: Int,
        cellWidth: Int,
        cellHeight: Int,
        paddingTop: Int,
        paddingBottom: Int,
        paddingLeft: Int,
        paddingRight: Int,
    )
    external fun nativeEncodeMouse(
        handle: Long,
        action: Int,
        button: Int,
        mods: Int,
        x: Float,
        y: Float,
        anyButtonPressed: Boolean,
        trackLastCell: Boolean,
    ): ByteArray?
    external fun nativeEncodeFocus(handle: Long, focused: Boolean): ByteArray?
    external fun nativeDrainPtyWrites(handle: Long): ByteArray
    external fun nativeSnapshotImages(handle: Long): ByteBuffer
    external fun nativeIsImageLoading(handle: Long): Boolean
    external fun nativeFormatSelectionRange(handle: Long, startCell: Int, endCell: Int): String?
    external fun nativeSelectWordAt(handle: Long, cellX: Int, cellY: Int): String?
    external fun nativeSelectLineAt(handle: Long, cellX: Int, cellY: Int): String?
    external fun nativeSelectAll(handle: Long): String?
}
