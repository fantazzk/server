package com.naminhyeok.fantazzk.template.dto

import io.swagger.v3.oas.annotations.media.Schema

@Schema(description = "템플릿에 포함된 선수 정보입니다.")
data class TemplatePlayerResponse(
    @field:Schema(description = "선수 이름입니다.", example = "김민수")
    val name: String,
    @field:Schema(description = "템플릿에 저장된 선수 순서입니다.", example = "0")
    val displayOrder: Int,
)
