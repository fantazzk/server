package com.naminhyeok.fantazzk.room.model

interface RoomModel : RoomIdentity, RoomProps, AuditProps

fun RoomModel.isWaiting(): Boolean = status == RoomStatus.WAITING

fun RoomModel.isInProgress(): Boolean = status == RoomStatus.IN_PROGRESS

fun RoomModel.isAuction(): Boolean = mode == TeamBuildingMode.AUCTION

fun RoomModel.isDraft(): Boolean = mode == TeamBuildingMode.DRAFT

val RoomModel.configuration: TeamBuildingConfiguration
    get() = TeamBuildingConfiguration.from(this)

val RoomModel.progress: RoomProgress
    get() = RoomProgress.from(this)

val RoomModel.picksPerTeam: Int get() = teamSize - 1
