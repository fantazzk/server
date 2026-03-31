package com.naminhyeok.fantazzk.room.web.dto

import io.swagger.v3.oas.annotations.media.Schema

@Schema(description = "경매 입찰 요청입니다.")
data class PlaceBidRequest(
    @field:Schema(description = "입찰을 수행할 팀장 ID 입니다. RoomResponse.teamLeaders[].id 값을 사용합니다.", example = "leader-02")
    val teamLeaderId: String,
    @field:Schema(description = "현재 최고가보다 큰 입찰 금액입니다.", example = "120")
    val amount: Int,
)
