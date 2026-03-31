package com.naminhyeok.fantazzk.room.web.dto

import io.swagger.v3.oas.annotations.media.Schema

@Schema(description = "방 참가 요청입니다.")
data class JoinRoomRequest(
    @field:Schema(description = "방에 참가할 팀장 닉네임입니다.", example = "참가자")
    val nickname: String,
)
