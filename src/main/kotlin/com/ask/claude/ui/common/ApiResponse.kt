package com.ask.claude.ui.common

data class ApiResponse<T>(
    val success: Boolean,
    val data: T?,
    val message: String?,
) {
    companion object {
        fun <T> success(data: T): ApiResponse<T> = ApiResponse(success = true, data = data, message = null)

        fun <T> error(message: String): ApiResponse<T> = ApiResponse(success = false, data = null, message = message)
    }
}
