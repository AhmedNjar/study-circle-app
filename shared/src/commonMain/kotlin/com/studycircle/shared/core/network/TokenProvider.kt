package com.studycircle.shared.core.network

// core stays feature-agnostic: it depends on this abstraction, not on the
// auth feature's SessionLocalDataSource directly. SessionLocalDataSource
// implements it in the auth feature's data layer — the dependency points
// the correct direction (feature -> core), never core -> feature.
fun interface TokenProvider {
    suspend fun currentToken(): String?
}
