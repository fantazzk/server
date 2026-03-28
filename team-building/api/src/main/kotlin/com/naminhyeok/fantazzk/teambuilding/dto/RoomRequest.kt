package com.naminhyeok.fantazzk.teambuilding.dto

data class CreateRoomRequest(
    val templateId: Long,
    val hostNickname: String,
)

data class JoinRoomRequest(
    val nickname: String,
)

data class PlaceBidRequest(
    val teamLeaderId: String,
    val amount: Int,
)

data class PickRequest(
    val teamLeaderId: String,
    val playerName: String,
)
