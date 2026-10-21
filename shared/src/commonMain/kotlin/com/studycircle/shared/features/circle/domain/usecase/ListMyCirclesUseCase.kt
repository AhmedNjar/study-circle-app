package com.studycircle.shared.features.circle.domain.usecase

import com.studycircle.shared.features.auth.domain.repository.AuthRepository
import com.studycircle.shared.features.circle.domain.model.Circle
import com.studycircle.shared.features.circle.domain.repository.CircleRepository

// Resolves "who is asking" from the persisted session so callers (ViewModels)
// don't thread a userId through every screen.
class ListMyCirclesUseCase(
    private val circles: CircleRepository,
    private val auth: AuthRepository
) {
    suspend operator fun invoke(): Result<List<Circle>> {
        val session = auth.currentSession()
            ?: return Result.failure(IllegalStateException("No active session"))
        return circles.listMine(session.userId)
    }
}
