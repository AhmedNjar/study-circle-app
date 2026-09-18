package com.studycircle.backend.streak

import org.springframework.data.jpa.repository.JpaRepository
import java.util.Optional

interface StreakRepository : JpaRepository<StreakEntity, StreakId> {
    fun findByCircleIdAndOwnerKey(circleId: String, ownerKey: String): Optional<StreakEntity>
}
