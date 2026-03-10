package com.titipin.modules.jastip


import kotlinx.serialization.Serializable

@Serializable
data class CreateJastipRequest(
    val fromLocation: String,
    val toLocation: String,
    val deadline: String,       // format: "2025-03-10T15:00:00"
    val latitude: Double,
    val longitude: Double,
    val notes: String? = null
)

@Serializable
data class JastipDto(
    val id: String,
    val userId: String,
    val fromLocation: String,
    val toLocation: String,
    val deadline: String,
    val latitude: Double,
    val longitude: Double,
    val notes: String? = null,
    val status: String,
    val createdAt: String
)