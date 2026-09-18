package com.studycircle.backend.streak.dto

import com.fasterxml.jackson.annotation.JsonSubTypes
import com.fasterxml.jackson.annotation.JsonTypeInfo

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.EXISTING_PROPERTY, property = "type")
@JsonSubTypes(
    JsonSubTypes.Type(value = StreakOwnerDto.Individual::class, name = "INDIVIDUAL"),
    JsonSubTypes.Type(value = StreakOwnerDto.Group::class, name = "GROUP")
)
sealed class StreakOwnerDto {
    abstract val type: String
    data class Individual(override val type: String = "INDIVIDUAL", val userId: String) : StreakOwnerDto()
    data class Group(override val type: String = "GROUP", val groupId: String) : StreakOwnerDto()
}

data class StreakResponse(
    val circleId: String,
    val owner: StreakOwnerDto,
    val currentCount: Int,
    val bestCount: Int,
    val lastCompletedDate: String?,
    val freezesAvailable: Int
)
