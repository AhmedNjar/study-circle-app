package com.studycircle.shared.features.circle.data.repository

import com.studycircle.shared.core.network.safeApiCall
import com.studycircle.shared.database.StudyCircleDatabase
import com.studycircle.shared.features.auth.domain.model.Role
import com.studycircle.shared.features.circle.data.remote.CircleApi
import com.studycircle.shared.features.circle.data.remote.dto.CircleResponseDto
import com.studycircle.shared.features.circle.data.remote.dto.CreateCircleRequestDto
import com.studycircle.shared.features.circle.data.remote.dto.JoinCircleRequestDto
import com.studycircle.shared.features.circle.domain.model.Circle
import com.studycircle.shared.features.circle.domain.model.CircleDetail
import com.studycircle.shared.features.circle.domain.model.CircleMemberSummary
import com.studycircle.shared.features.circle.domain.repository.CircleRepository
import kotlinx.datetime.Instant

class CircleRepositoryImpl(
    private val api: CircleApi,
    private val database: StudyCircleDatabase
) : CircleRepository {
    private val queries = database.circleQueries

    override suspend fun create(name: String): Result<Circle> =
        safeApiCall { api.create(CreateCircleRequestDto(name)) }
            .map { dto -> dto.also(::cache).toDomain() }

    override suspend fun join(joinCode: String): Result<Circle> =
        safeApiCall { api.join(JoinCircleRequestDto(joinCode)) }
            .map { dto -> dto.also(::cache).toDomain() }

    override suspend fun getDetail(circleId: String): Result<CircleDetail> =
        safeApiCall { api.getDetail(circleId) }
            .map { dto ->
                // cache the flattened member-id view too, so a later offline
                // list/detail read still has something to show
                queries.insertOrReplaceCircle(dto.id, dto.name, dto.joinCode, dto.supervisorId, dto.createdAt, if (dto.isActive) 1L else 0L)
                dto.members.forEach { queries.insertMember(dto.id, it.id) }

                CircleDetail(
                    id = dto.id,
                    name = dto.name,
                    joinCode = dto.joinCode,
                    supervisorId = dto.supervisorId,
                    members = dto.members.map { CircleMemberSummary(it.id, it.displayName, Role.valueOf(it.role)) },
                    createdAt = Instant.parse(dto.createdAt),
                    isActive = dto.isActive
                )
            }

    private fun cache(dto: CircleResponseDto) {
        queries.insertOrReplaceCircle(dto.id, dto.name, dto.joinCode, dto.supervisorId, dto.createdAt, if (dto.isActive) 1L else 0L)
        dto.memberIds.forEach { queries.insertMember(dto.id, it) }
    }

    private fun CircleResponseDto.toDomain() = Circle(
        id = id, name = name, joinCode = joinCode, supervisorId = supervisorId,
        memberIds = memberIds, createdAt = Instant.parse(createdAt), isActive = isActive
    )
}
