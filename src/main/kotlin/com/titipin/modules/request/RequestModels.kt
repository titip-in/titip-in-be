package com.titipin.modules.request

import kotlinx.serialization.Serializable

@Serializable
data class CreateRequestRequest(
    val fromLocation: String,
    val toLocation: String,
    val notes: String? = null
)

@Serializable
data class UserSummary(
    val name: String,
    val waNumber: String,
    val avatarUrl: String? = null
)

@Serializable
data class RequestDto(
    val id: String,
    val userId: String,
    val user: UserSummary,
    val fromLocation: String,
    val toLocation: String,
    val notes: String? = null,
    val status: String,
    val createdAt: String
)

@Serializable
data class TakeRequestResponse(
    val request: RequestDto,
    val takenBy: UserSummary
)