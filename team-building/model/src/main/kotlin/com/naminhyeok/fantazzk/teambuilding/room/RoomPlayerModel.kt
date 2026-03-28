package com.naminhyeok.fantazzk.teambuilding.room

import com.naminhyeok.fantazzk.teambuilding.AuditProps

interface RoomPlayerModel : RoomPlayerIdentity, RoomPlayerProps, AuditProps

fun RoomPlayerModel.isAvailable(): Boolean = status == PlayerStatus.AVAILABLE
