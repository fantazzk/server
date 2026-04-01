package com.naminhyeok.fantazzk.room.dto

import io.swagger.v3.oas.annotations.media.Schema

@Schema(description = "드래프트 픽 요청입니다.")
data class PickRequest(
    @field:Schema(description = "픽을 수행할 팀장 ID 입니다. RoomResponse.teamLeaders[].id 값을 사용합니다.", example = "leader-01")
    val teamLeaderId: String,
    @field:Schema(description = "현재 턴에 선택할 선수 이름입니다.", example = "김민수")
    val playerName: String,
)
