package com.studycircle.shared.features.streak.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
sealed class StreakOwnerDto {
    @Serializable
    @SerialName("INDIVIDUAL")
    data class Individual(val userId: String) : StreakOwnerDto()

    @Serializable
    @SerialName("GROUP")
    data class Group(val groupId: String) : StreakOwnerDto()
}

@Serializable
data class StreakResponseDto(
    val circleId: String,
    val owner: StreakOwnerDto,
    val currentCount: Int,
    val bestCount: Int,
    val lastCompletedDate: String? = null,
    val freezesAvailable: Int = 0
)
