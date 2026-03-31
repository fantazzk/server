package com.naminhyeok.fantazzk.room.web.dto

import io.swagger.v3.oas.annotations.media.ArraySchema
import io.swagger.v3.oas.annotations.media.Schema

@Schema(description = "페이지 번호 기반 목록 응답입니다.")
data class PagedResponse<T>(
    @field:ArraySchema(arraySchema = Schema(description = "현재 페이지의 결과 목록입니다."))
    val results: List<T>,
    @field:Schema(description = "현재 페이지 메타데이터입니다.")
    val paging: Paging,
) {
    @Schema(description = "페이지 번호 기반 목록 메타데이터입니다.")
    data class Paging(
        @field:Schema(description = "현재 페이지 번호입니다.", example = "0")
        val pageNumber: Int,
        @field:Schema(description = "페이지 크기입니다.", example = "20")
        val pageSize: Int,
        @field:Schema(description = "다음 페이지 존재 여부입니다.", example = "true")
        val hasNext: Boolean,
        @field:Schema(description = "전체 결과 수입니다.", example = "52")
        val totalCount: Long,
    )
}
