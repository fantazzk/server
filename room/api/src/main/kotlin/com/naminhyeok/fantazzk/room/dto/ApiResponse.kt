package com.naminhyeok.fantazzk.room.dto

data class ApiResponse<T>(
    val resultType: ResultType,
    val success: T?,
    val error: ErrorResponse?,
) {
    enum class ResultType { SUCCESS, ERROR }

    data class ErrorResponse(
        val status: Int,
        val errorCode: String,
        val reason: String,
        val data: Map<String, Any>?,
    )

    companion object {
        fun <T> success(data: T): ApiResponse<T> = ApiResponse(ResultType.SUCCESS, success = data, error = null)

        fun error(
            status: Int,
            errorCode: String,
            reason: String,
            data: Map<String, Any>? = null,
        ): ApiResponse<Nothing> = ApiResponse(ResultType.ERROR, success = null, error = ErrorResponse(status, errorCode, reason, data))
    }
}
