package com.studycircle.shared.features.circle.domain.repository

import com.studycircle.shared.features.circle.domain.model.Circle
import com.studycircle.shared.features.circle.domain.model.CircleDetail

interface CircleRepository {
    suspend fun create(name: String): Result<Circle>
    suspend fun join(joinCode: String): Result<Circle>
    suspend fun getDetail(circleId: String): Result<CircleDetail>
}
