package com.naminhyeok.fantazzk.room

interface RoomModel : RoomIdentity, RoomProps, AuditProps

fun Room.isWaiting(): Boolean = status == RoomStatus.WAITING

fun Room.isInProgress(): Boolean = status == RoomStatus.IN_PROGRESS

fun Room.isAuction(): Boolean = mode == TeamBuildingMode.AUCTION

fun Room.isDraft(): Boolean = mode == TeamBuildingMode.DRAFT

val Room.configuration: TeamBuildingConfiguration
    get() = TeamBuildingConfiguration.from(this)

val Room.progress: RoomProgress
    get() = RoomProgress.from(this)

val Room.picksPerTeam: Int get() = teamSize - 1

fun RoomModel.isWaiting(): Boolean = status == RoomStatus.WAITING

fun RoomModel.isInProgress(): Boolean = status == RoomStatus.IN_PROGRESS

fun RoomModel.isAuction(): Boolean = mode == TeamBuildingMode.AUCTION

fun RoomModel.isDraft(): Boolean = mode == TeamBuildingMode.DRAFT

val RoomModel.configuration: TeamBuildingConfiguration
    get() = TeamBuildingConfiguration.from(this)

val RoomModel.progress: RoomProgress
    get() = RoomProgress.from(this)

val RoomModel.picksPerTeam: Int get() = teamSize - 1

fun RoomModel.requireCurrentAuctionRound(): Int = Room.from(this).requireCurrentAuctionRound()

fun RoomModel.requireCurrentTurnIndex(): Int = Room.from(this).requireCurrentTurnIndex()

fun RoomModel.advanceAuction(
    nextRound: Int,
    completed: Boolean,
): Room = Room.from(this).advanceAuction(nextRound = nextRound, completed = completed)

fun RoomModel.moveAuctionTargetToNextRound(nextRound: Int): Room = Room.from(this).moveAuctionTargetToNextRound(nextRound = nextRound)

fun RoomModel.advanceDraftTurn(
    nextTurnIndex: Int,
    completed: Boolean,
): Room = Room.from(this).advanceDraftTurn(nextTurnIndex = nextTurnIndex, completed = completed)
