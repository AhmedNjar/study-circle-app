package com.studycircle.shared.features.auth.domain.repository

import com.studycircle.shared.features.auth.domain.model.Role
import com.studycircle.shared.features.auth.domain.model.Session
import kotlinx.coroutines.flow.Flow

interface AuthRepository {
    suspend fun register(username: String, password: String, displayName: String, role: Role): Result<Session>
    suspend fun login(username: String, password: String): Result<Session>
    suspend fun logout()
    fun observeSession(): Flow<Session?>
    suspend fun currentSession(): Session?
}
