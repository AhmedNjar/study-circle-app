package com.studycircle.backend.circle

import com.studycircle.backend.circle.dto.CircleDetailResponse
import com.studycircle.backend.circle.dto.CircleResponse
import com.studycircle.backend.circle.dto.MemberSummary
import com.studycircle.backend.common.AlreadyMemberException
import com.studycircle.backend.common.CircleInactiveException
import com.studycircle.backend.common.CircleNotFoundException
import com.studycircle.backend.common.NotCircleMemberException
import com.studycircle.backend.user.UserRepository
import org.springframework.stereotype.Service
import java.time.Instant
import java.util.UUID
import kotlin.random.Random

@Service
class CircleService(
    private val circleRepository: CircleRepository,
    private val memberRepository: CircleMemberRepository,
    private val userRepository: UserRepository
) {
    fun create(name: String, supervisorId: String): CircleResponse {
        val circle = CircleEntity(
            id = UUID.randomUUID().toString(),
            name = name,
            joinCode = generateUniqueJoinCode(),
            supervisorId = supervisorId,
            createdAt = Instant.now(),
            isActive = true
        )
        circleRepository.save(circle)
        return circle.toResponse(memberIds = emptyList())
    }

    fun join(joinCode: String, studentId: String): CircleResponse {
        val circle = circleRepository.findByJoinCode(joinCode).orElseThrow { CircleNotFoundException() }
        if (!circle.isActive) throw CircleInactiveException()
        if (memberRepository.existsByCircleIdAndUserId(circle.id, studentId)) throw AlreadyMemberException()

        memberRepository.save(CircleMemberEntity(circleId = circle.id, userId = studentId))
        return circle.toResponse(memberIds = currentMemberIds(circle.id))
    }

    // Every circle the caller supervises OR belongs to. Role isn't consulted: a
    // supervisor has supervisorId matches, a student has membership rows, and the
    // union is correct for both without a role branch.
    fun listMine(userId: String): List<CircleResponse> {
        val supervised = circleRepository.findBySupervisorId(userId)
        val memberOfIds = memberRepository.findByUserId(userId).map { it.circleId }
        val joined = if (memberOfIds.isEmpty()) emptyList() else circleRepository.findAllById(memberOfIds)
        return (supervised + joined)
            .distinctBy { it.id }
            .sortedByDescending { it.createdAt }
            .map { it.toResponse(memberIds = currentMemberIds(it.id)) }
    }

    fun getDetail(circleId: String, requesterId: String): CircleDetailResponse {
        val circle = circleRepository.findById(circleId).orElseThrow { CircleNotFoundException() }
        val memberIds = currentMemberIds(circle.id)

        val isSupervisor = circle.supervisorId == requesterId
        if (!isSupervisor && requesterId !in memberIds) throw NotCircleMemberException()

        val members = userRepository.findAllById(memberIds).map {
            MemberSummary(it.id, it.displayName, it.role)
        }
        return CircleDetailResponse(
            id = circle.id,
            name = circle.name,
            joinCode = circle.joinCode,
            supervisorId = circle.supervisorId,
            members = members,
            createdAt = circle.createdAt,
            isActive = circle.isActive
        )
    }

    private fun currentMemberIds(circleId: String) = memberRepository.findByCircleId(circleId).map { it.userId }

    private fun generateUniqueJoinCode(): String {
        val chars = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789" // no O/0/I/1 — avoids visual ambiguity
        var code: String
        do {
            code = (1..6).map { chars[Random.nextInt(chars.length)] }.joinToString("")
        } while (circleRepository.existsByJoinCode(code))
        return code
    }

    private fun CircleEntity.toResponse(memberIds: List<String>) =
        CircleResponse(id, name, joinCode, supervisorId, memberIds, createdAt, isActive)
}
