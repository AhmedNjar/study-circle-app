package com.studycircle.shared.features.auth.data.local

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToOneOrNull
import com.studycircle.shared.core.network.TokenProvider
import com.studycircle.shared.database.StudyCircleDatabase
import com.studycircle.shared.features.auth.domain.model.Role
import com.studycircle.shared.features.auth.domain.model.Session
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

// Implements TokenProvider so HttpClientFactory can read the current token
// through core's abstraction without core knowing this class exists.
class SessionLocalDataSource(database: StudyCircleDatabase) : TokenProvider {
    private val queries = database.sessionQueries

    fun observe(): Flow<Session?> =
        queries.selectSession()
            .asFlow()
            .mapToOneOrNull(Dispatchers.Default)
            .map { it?.toDomain() }

    fun save(session: Session) {
        queries.insertOrReplace(
            token = session.token,
            userId = session.userId,
            username = session.username,
            displayName = session.displayName,
            role = session.role.name
        )
    }

    fun clear() = queries.clear()

    fun currentOrNull(): Session? = queries.selectSession().executeAsOneOrNull()?.toDomain()

    override suspend fun currentToken(): String? = currentOrNull()?.token

    private fun com.studycircle.shared.database.SessionEntity.toDomain() =
        Session(token, userId, username, displayName, Role.valueOf(role))
}
