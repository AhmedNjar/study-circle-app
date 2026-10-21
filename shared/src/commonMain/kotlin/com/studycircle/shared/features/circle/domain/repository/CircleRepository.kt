package com.studycircle.shared.features.circle.domain.repository

import com.studycircle.shared.features.circle.domain.model.Circle
import com.studycircle.shared.features.circle.domain.model.CircleDetail

interface CircleRepository {
    suspend fun create(name: String): Result<Circle>
    suspend fun join(joinCode: String): Result<Circle>
    suspend fun getDetail(circleId: String): Result<CircleDetail>

    // Circles the given user supervises or belongs to. Network first; if the
    // network call fails, falls back to the local cache (scoped to userId) so the
    // home screen still opens offline. Fails only if offline AND nothing is cached.
    suspend fun listMine(userId: String): Result<List<Circle>>
}
