package com.naminhyeok.fantazzk.teambuilding.room

import com.naminhyeok.fantazzk.teambuilding.TeamBuildingMode

fun RoomModel.isWaiting(): Boolean = status == RoomStatus.WAITING

fun RoomModel.isInProgress(): Boolean = status == RoomStatus.IN_PROGRESS

fun RoomModel.isAuction(): Boolean = mode == TeamBuildingMode.AUCTION

fun RoomModel.isDraft(): Boolean = mode == TeamBuildingMode.DRAFT

val RoomModel.picksPerTeam: Int get() = teamSize - 1

fun RoomTeamLeaderModel.validateBudget(amount: Int) {
    val current = requireNotNull(remainingBudget) { "이 모드에서는 예산이 존재하지 않습니다" }
    require(amount <= current) { "예산이 부족합니다: 잔여 $current, 필요 $amount" }
}

fun RoomPlayerModel.isAvailable(): Boolean = status == PlayerStatus.AVAILABLE
