package com.studycircle.shared.core.platform

import java.io.File

actual object PlatformFileReader {
    actual suspend fun readBytes(path: String): ByteArray = File(path).readBytes()
}
