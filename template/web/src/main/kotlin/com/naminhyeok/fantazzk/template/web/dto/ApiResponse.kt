package com.naminhyeok.fantazzk.template.web.dto

import io.swagger.v3.oas.annotations.media.Schema

@Schema(description = "공통 API 응답 envelope 입니다. 성공 시 success 를, 실패 시 error 를 사용합니다.")
data class ApiResponse<T>(
    @field:Schema(description = "응답 종류입니다.", example = "SUCCESS")
    val resultType: ResultType,
    @field:Schema(description = "성공 응답 payload 입니다. resultType 이 SUCCESS 일 때만 값이 있습니다.", nullable = true)
    val success: T?,
    @field:Schema(description = "실패 응답 정보입니다. resultType 이 ERROR 일 때만 값이 있습니다.", nullable = true)
    val error: ErrorResponse?,
) {
    enum class ResultType { SUCCESS, ERROR }

    @Schema(description = "실패 응답 상세입니다.")
    data class ErrorResponse(
        @field:Schema(description = "HTTP status code 입니다.", example = "404")
        val status: Int,
        @field:Schema(description = "클라이언트가 분기 처리할 애플리케이션 에러 코드입니다.", example = "TEMPLATE_NOT_FOUND")
        val errorCode: String,
        @field:Schema(description = "사람이 읽을 수 있는 실패 사유입니다.", example = "템플릿을 찾을 수 없습니다")
        val reason: String,
        @field:Schema(description = "추가 오류 정보가 있을 때만 포함됩니다.", nullable = true)
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
