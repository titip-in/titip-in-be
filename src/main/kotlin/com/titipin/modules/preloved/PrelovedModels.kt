package com.titipin.modules.preloved

import kotlinx.serialization.Serializable

@Serializable
data class CreatePrelovedRequest(
    val title: String,
    val description: String? = null,
    val price: Double,
    val category: String,
    val condition: String,  // NEW, LIKE_NEW, GOOD, FAIR
    val imageUrl: String? = null
)

@Serializable
data class PrelovedDto(
    val id: String,
    val userId: String,
    val title: String,
    val description: String? = null,
    val price: Double,
    val category: String,
    val condition: String,
    val imageUrl: String? = null,
    val status: String,
    val createdAt: String
)
@Serializable
data class UpdatePrelovedRequest(
    val status: String  // AVAILABLE, SOLD, RESERVED
)