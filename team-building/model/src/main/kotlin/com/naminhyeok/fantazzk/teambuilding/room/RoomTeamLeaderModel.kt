package com.naminhyeok.fantazzk.teambuilding.room

import com.naminhyeok.fantazzk.teambuilding.AuditProps

interface RoomTeamLeaderModel : RoomTeamLeaderIdentity, RoomTeamLeaderProps, AuditProps

fun RoomTeamLeaderModel.validateBudget(amount: Int) {
    val current = requireNotNull(remainingBudget) { "이 모드에서는 예산이 존재하지 않습니다" }
    require(amount <= current) { "예산이 부족합니다: 잔여 $current, 필요 $amount" }
}
