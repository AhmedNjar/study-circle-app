package com.studycircle.backend.circle

import org.springframework.data.jpa.repository.JpaRepository
import java.util.Optional

interface CircleRepository : JpaRepository<CircleEntity, String> {
    fun findByJoinCode(joinCode: String): Optional<CircleEntity>
    fun existsByJoinCode(joinCode: String): Boolean
    fun findBySupervisorId(supervisorId: String): List<CircleEntity>
}

interface CircleMemberRepository : JpaRepository<CircleMemberEntity, CircleMemberId> {
    fun findByCircleId(circleId: String): List<CircleMemberEntity>
    fun findByUserId(userId: String): List<CircleMemberEntity>
    fun existsByCircleIdAndUserId(circleId: String, userId: String): Boolean
}
