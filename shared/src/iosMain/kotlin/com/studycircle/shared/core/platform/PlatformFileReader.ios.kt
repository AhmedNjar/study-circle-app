package com.studycircle.shared.core.platform

import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.usePinned
import platform.Foundation.NSData
import platform.Foundation.dataWithContentsOfFile
import platform.posix.memcpy

// NOTE: unverified — this compiles against a plausible reading of the
// NSData/Kotlin-Native interop API, but there's no iOS app shell yet to
// actually run it against (see GETTING_STARTED.md). Treat this file as the
// first thing to sanity-check once iOS work starts, not as trusted code.
@OptIn(ExperimentalForeignApi::class)
actual object PlatformFileReader {
    actual suspend fun readBytes(path: String): ByteArray {
        val data = NSData.dataWithContentsOfFile(path) ?: error("File not found at $path")
        val size = data.length.toInt()
        val bytes = ByteArray(size)
        if (size > 0) {
            bytes.usePinned { pinned ->
                memcpy(pinned.addressOf(0), data.bytes, data.length)
            }
        }
        return bytes
    }
}
