package com.naminhyeok.fantazzk.room.web

import io.swagger.v3.oas.annotations.media.Schema

@Schema(description = "방에 참가한 팀장 정보입니다.")
data class TeamLeaderResponse(
    @field:Schema(description = "팀장 식별자입니다. 이후 입찰이나 드래프트 픽 요청에서 사용합니다.", example = "leader-01")
    val id: String,
    @field:Schema(description = "팀장 닉네임입니다.", example = "호스트")
    val nickname: String,
    @field:Schema(description = "경매 모드일 때만 남은 예산이 포함됩니다.", example = "300", nullable = true)
    val remainingBudget: Int?,
)
