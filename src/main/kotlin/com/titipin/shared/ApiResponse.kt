package com.titipin.shared

import kotlinx.serialization.Serializable

@Serializable
data class ApiResponse<T>(
    val success: Boolean,
    val message: String? = null,
    val data: T? = null,
    val error: ApiError? = null,
    val meta: ApiMeta? = null
) {
    companion object {
        fun <T> success(data: T? = null, message: String = "OK"): ApiResponse<T> {
            return ApiResponse(
                success = true,
                message = message,
                data = data
            )
        }

        fun <T> paginated(data: T, meta: ApiMeta, message: String = "OK"): ApiResponse<T> {
            return ApiResponse(
                success = true,
                message = message,
                data = data,
                meta = meta
            )
        }

        fun <T> error(code: String, message: String): ApiResponse<T> {
            return ApiResponse(
                success = false,
                error = ApiError(code, message)
            )
        }
    }
}

@Serializable
data class ApiError(
    val code: String,
    val message: String
)

@Serializable
data class ApiMeta(
    val page: Int,
    val limit: Int,
    val total: Int,
    val hasNext: Boolean
)