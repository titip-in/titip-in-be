package com.titipin.modules.wanted

import kotlinx.serialization.Serializable

@Serializable
data class CreateWantedRequest(
    val title: String,
    val description: String? = null,
    val maxPrice: Double? = null,
    val category: String? = null
)

@Serializable
data class UserSummary(
    val name: String,
    val waNumber: String,
    val avatarUrl: String? = null
)

@Serializable
data class WantedDto(
    val id: String,
    val userId: String,
    val user: UserSummary,
    val title: String,
    val description: String?,
    val maxPrice: Double?,
    val category: String?,
    val status: String,
    val createdAt: String
)

@Serializable
data class FulfillWantedResponse(
    val wantedItem: WantedDto,
    val foundBy: UserSummary
)