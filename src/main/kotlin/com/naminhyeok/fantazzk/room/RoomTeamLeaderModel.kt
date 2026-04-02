package com.naminhyeok.fantazzk.room

interface RoomTeamLeaderModel : RoomTeamLeaderIdentity, RoomTeamLeaderProps, AuditProps

fun RoomTeamLeader.validateBudget(amount: Int) = requireCanBid(amount)

fun RoomTeamLeaderModel.requireCanBid(amount: Int) = RoomTeamLeader.from(this).requireCanBid(amount)

fun RoomTeamLeaderModel.spend(amount: Int): RoomTeamLeader = RoomTeamLeader.from(this).spend(amount)

fun RoomTeamLeaderModel.validateBudget(amount: Int) = requireCanBid(amount)
