package com.studycircle.backend.streak

import com.fasterxml.jackson.databind.ObjectMapper
import com.studycircle.backend.streak.dto.StreakOwnerDto
import com.studycircle.backend.streak.dto.StreakResponse
import org.springframework.stereotype.Service
import java.time.LocalDate

@Service
class StreakService(
    private val streakRepository: StreakRepository,
    private val objectMapper: ObjectMapper
) {
    // "owner" on the wire is a bare id with no type tag (per the step-2
    // contract), so a lookup tries the individual key first, then the group
    // key. IDs are UUIDs — globally unique regardless of entity type — so
    // this never has a real ambiguity, just two lookups instead of one.
    fun getForOwner(circleId: String, owner: String): StreakResponse {
        streakRepository.findByCircleIdAndOwnerKey(circleId, individualKey(owner)).orElse(null)?.let { return it.toResponse() }
        streakRepository.findByCircleIdAndOwnerKey(circleId, groupKey(owner)).orElse(null)?.let { return it.toResponse() }
        return StreakResponse(
            circleId = circleId,
            owner = StreakOwnerDto.Individual(userId = owner),
            currentCount = 0, bestCount = 0, lastCompletedDate = null, freezesAvailable = 0
        )
    }

    // Called by SubmissionService the moment an INDIVIDUAL submission is
    // APPROVED. completionDate is the submission's own date, not the
    // approval's — see the Phase 3 note in StreakMath.kt.
    fun recordCompletion(circleId: String, userId: String, completionDate: LocalDate) {
        apply(circleId, individualKey(userId), StreakOwnerDto.Individual(userId = userId), completionDate)
    }

    // Called by SubmissionService once a GROUP task's completionRule is
    // satisfied (all-or-any members approved, depending on the group's
    // rule) — see SubmissionService.recordGroupStreakIfComplete. The whole
    // group shares one collective الرِّباط, not one each.
    fun recordGroupCompletion(circleId: String, groupId: String, completionDate: LocalDate) {
        apply(circleId, groupKey(groupId), StreakOwnerDto.Group(groupId = groupId), completionDate)
    }

    private fun apply(circleId: String, ownerKey: String, owner: StreakOwnerDto, completionDate: LocalDate) {
        val existing = streakRepository.findByCircleIdAndOwnerKey(circleId, ownerKey).orElse(null)
        val entity = existing ?: StreakEntity(
            circleId = circleId, ownerKey = ownerKey,
            ownerJson = objectMapper.writeValueAsString(owner),
            currentCount = 0, bestCount = 0, lastCompletedDate = null
        )
        val result = StreakMath.applyCompletion(entity.currentCount, entity.bestCount, entity.lastCompletedDate, completionDate)
        entity.currentCount = result.currentCount
        entity.bestCount = result.bestCount
        entity.lastCompletedDate = result.lastCompletedDate
        streakRepository.save(entity)
    }

    private fun individualKey(userId: String) = "individual:$userId"
    private fun groupKey(groupId: String) = "group:$groupId"

    private fun StreakEntity.toResponse() = StreakResponse(
        circleId = circleId,
        owner = objectMapper.readValue(ownerJson, StreakOwnerDto::class.java),
        currentCount = currentCount, bestCount = bestCount,
        lastCompletedDate = lastCompletedDate?.toString(), freezesAvailable = freezesAvailable
    )
}
