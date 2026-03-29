package com.naminhyeok.fantazzk.room.dto

import io.swagger.v3.oas.annotations.media.ArraySchema
import io.swagger.v3.oas.annotations.media.Schema

@Schema(description = "커서 기반 목록 응답입니다.")
data class CursorResponse<T>(
    @field:ArraySchema(arraySchema = Schema(description = "현재 커서 구간의 결과 목록입니다."))
    val results: List<T>,
    @field:Schema(description = "다음 페이지 조회에 사용할 커서입니다. 마지막 페이지면 null 입니다.", nullable = true)
    val next: String?,
    @field:Schema(description = "전체 결과 수가 계산 가능한 경우에만 포함됩니다.", nullable = true)
    val totalCount: Long?,
)
