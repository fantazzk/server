package com.naminhyeok.fantazzk.room

interface RoomPlayerModel : RoomPlayerIdentity, RoomPlayerProps, AuditProps

fun RoomPlayerModel.isAvailable(): Boolean = status == PlayerStatus.AVAILABLE
