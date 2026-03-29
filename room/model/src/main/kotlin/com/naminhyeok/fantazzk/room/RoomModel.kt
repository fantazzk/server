package com.naminhyeok.fantazzk.room

interface RoomModel : RoomIdentity, RoomProps, AuditProps

fun RoomModel.isWaiting(): Boolean = status == RoomStatus.WAITING

fun RoomModel.isInProgress(): Boolean = status == RoomStatus.IN_PROGRESS

fun RoomModel.isAuction(): Boolean = mode == TeamBuildingMode.AUCTION

fun RoomModel.isDraft(): Boolean = mode == TeamBuildingMode.DRAFT

val RoomModel.picksPerTeam: Int get() = teamSize - 1
