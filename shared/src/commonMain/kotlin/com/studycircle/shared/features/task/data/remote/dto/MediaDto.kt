package com.studycircle.shared.features.task.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class UploadUrlRequestDto(val mediaType: String, val extension: String)

@Serializable
data class UploadUrlResponseDto(val uploadUrl: String, val remoteMediaUrl: String)
