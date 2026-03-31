package com.naminhyeok.fantazzk.room.model

interface RoomPlayerModel : RoomPlayerIdentity, RoomPlayerProps, AuditProps

fun RoomPlayerModel.isAvailable(): Boolean = status == PlayerStatus.AVAILABLE

fun RoomPlayerModel.assign(): RoomPlayer = RoomPlayer.from(this).assign()

fun RoomPlayerModel.moveToBack(displayOrder: Int): RoomPlayer = RoomPlayer.from(this).moveToBack(displayOrder)
