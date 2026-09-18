package com.studycircle.shared.features.auth.data.repository

import com.studycircle.shared.core.network.safeApiCall
import com.studycircle.shared.features.auth.data.local.SessionLocalDataSource
import com.studycircle.shared.features.auth.data.remote.AuthApi
import com.studycircle.shared.features.auth.data.remote.dto.AuthResponseDto
import com.studycircle.shared.features.auth.data.remote.dto.LoginRequestDto
import com.studycircle.shared.features.auth.data.remote.dto.RegisterRequestDto
import com.studycircle.shared.features.auth.domain.model.Role
import com.studycircle.shared.features.auth.domain.model.Session
import com.studycircle.shared.features.auth.domain.repository.AuthRepository
import kotlinx.coroutines.flow.Flow

class AuthRepositoryImpl(
    private val api: AuthApi,
    private val local: SessionLocalDataSource
) : AuthRepository {

    override suspend fun register(
        username: String, password: String, displayName: String, role: Role
    ): Result<Session> =
        safeApiCall { api.register(RegisterRequestDto(username, password, displayName, role.name)) }
            .map { it.toSession().also(local::save) }

    override suspend fun login(username: String, password: String): Result<Session> =
        safeApiCall { api.login(LoginRequestDto(username, password)) }
            .map { it.toSession().also(local::save) }

    override suspend fun logout() = local.clear()

    override fun observeSession(): Flow<Session?> = local.observe()

    override suspend fun currentSession(): Session? = local.currentOrNull()

    private fun AuthResponseDto.toSession() = Session(
        token = token,
        userId = user.id,
        username = user.username,
        displayName = user.displayName,
        role = Role.valueOf(user.role)
    )
}
