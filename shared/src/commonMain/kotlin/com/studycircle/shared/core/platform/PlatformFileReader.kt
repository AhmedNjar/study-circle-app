package com.studycircle.shared.core.platform

// Reads a locally-cached file's bytes for upload — needed because Media
// evidence stores a localCachePath, not the bytes themselves, and file I/O
// has no commonMain API in KMP.
expect object PlatformFileReader {
    suspend fun readBytes(path: String): ByteArray
}
