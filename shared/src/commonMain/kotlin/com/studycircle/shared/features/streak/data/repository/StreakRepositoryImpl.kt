package com.studycircle.shared.features.streak.data.repository

import com.studycircle.shared.core.network.safeApiCall
import com.studycircle.shared.database.StudyCircleDatabase
import com.studycircle.shared.features.streak.data.remote.StreakApi
import com.studycircle.shared.features.streak.data.remote.dto.StreakOwnerDto
import com.studycircle.shared.features.streak.data.remote.dto.StreakResponseDto
import com.studycircle.shared.features.streak.domain.model.Streak
import com.studycircle.shared.features.streak.domain.model.StreakOwner
import com.studycircle.shared.features.streak.domain.repository.StreakRepository
import kotlinx.datetime.LocalDate
import kotlinx.serialization.json.Json

class StreakRepositoryImpl(
    private val api: StreakApi,
    private val database: StudyCircleDatabase
) : StreakRepository {
    private val queries = database.streakQueries

    override suspend fun getStreak(circleId: String, owner: StreakOwner): Result<Streak> {
        val ownerParam = when (owner) {
            is StreakOwner.Individual -> owner.userId
            is StreakOwner.Group -> owner.groupId
        }
        return safeApiCall { api.get(circleId, ownerParam) }
            .map { dto -> dto.also(::cache).toDomain() }
    }

    private fun cache(dto: StreakResponseDto) {
        queries.insertOrReplace(
            dto.circleId, ownerKeyFor(dto.owner),
            Json.encodeToString(StreakOwnerDto.serializer(), dto.owner),
            dto.currentCount.toLong(), dto.bestCount.toLong(),
            dto.lastCompletedDate, dto.freezesAvailable.toLong()
        )
    }

    private fun ownerKeyFor(owner: StreakOwnerDto): String = when (owner) {
        is StreakOwnerDto.Individual -> "individual:${owner.userId}"
        is StreakOwnerDto.Group -> "group:${owner.groupId}"
    }
}

internal fun StreakOwnerDto.toDomain(): StreakOwner = when (this) {
    is StreakOwnerDto.Individual -> StreakOwner.Individual(userId)
    is StreakOwnerDto.Group -> StreakOwner.Group(groupId)
}

internal fun StreakResponseDto.toDomain() = Streak(
    circleId = circleId,
    owner = owner.toDomain(),
    currentCount = currentCount,
    bestCount = bestCount,
    lastCompletedDate = lastCompletedDate?.let { LocalDate.parse(it) },
    freezesAvailable = freezesAvailable
)
