package com.naminhyeok.fantazzk.room.web

import io.swagger.v3.oas.annotations.media.Schema

@Schema(description = "방 생성 요청입니다.")
data class CreateRoomRequest(
    @field:Schema(description = "방 생성에 사용할 템플릿 ID 입니다.", example = "123e4567-e89b-12d3-a456-426614174000")
    val templateId: String,
    @field:Schema(description = "첫 번째 팀장으로 등록될 호스트 닉네임입니다.", example = "호스트")
    val hostNickname: String,
)
