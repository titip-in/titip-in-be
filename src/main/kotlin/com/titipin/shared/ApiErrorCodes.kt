package com.titipin.shared

object ApiErrorCodes {
    // global
    const val BAD_REQUEST = "BAD_REQUEST"
    const val UNAUTHORIZED = "UNAUTHORIZED"
    const val FORBIDDEN = "FORBIDDEN"
    const val NOT_FOUND = "NOT_FOUND"
    const val CONFLICT = "CONFLICT"
    const val RATE_LIMITED = "RATE_LIMITED"
    const val SERVER_ERROR = "SERVER_ERROR"

    // auth
    const val EMAIL_ALREADY_EXISTS = "EMAIL_ALREADY_EXISTS"
    const val INVALID_CREDENTIALS = "INVALID_CREDENTIALS"
    const val TOKEN_EXPIRED = "TOKEN_EXPIRED"

    // jastip/preloved
    const val INSUFFICIENT_PERMISSION = "INSUFFICIENT_PERMISSION"
    const val ITEM_NOT_AVAILABLE = "ITEM_NOT_AVAILABLE"
    const val STATUS_UPDATE_FAILED = "STATUS_UPDATE_FAILED"
}