package com.titipin.modules.auth

import kotlinx.serialization.Serializable

// Request register
@Serializable
data class RegisterRequest(
    val name: String,
    val email: String,
    val password: String,
    val waNumber: String
)

// Request login
@Serializable
data class LoginRequest(
    val email: String,
    val password: String
)


@Serializable
data class AuthResponse(
    val accessToken: String,
    val user: UserDto
)


@Serializable
data class UserDto(
    val id: String,
    val name: String,
    val email: String,
    val waNumber: String,
    val avatarUrl: String? = null
)