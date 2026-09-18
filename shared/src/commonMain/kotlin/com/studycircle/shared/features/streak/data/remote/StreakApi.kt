package com.studycircle.shared.features.streak.data.remote

import com.studycircle.shared.features.streak.data.remote.dto.StreakResponseDto
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.parameter

class StreakApi(private val client: HttpClient) {
    // "owner" travels as a bare userId/groupId string on the wire, per the
    // step-2 contract — not the serialized StreakOwnerDto shape, which is
    // only how the *response* describes who the streak belongs to.
    suspend fun get(circleId: String, owner: String): StreakResponseDto =
        client.get("/api/v1/circles/$circleId/streaks") {
            parameter("owner", owner)
        }.body()
}
