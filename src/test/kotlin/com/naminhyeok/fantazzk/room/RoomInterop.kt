package com.naminhyeok.fantazzk.room

import com.naminhyeok.fantazzk.room.domain.RoomBidId
import com.naminhyeok.fantazzk.room.domain.RoomPlayerId
import com.naminhyeok.fantazzk.room.domain.RoomTeamLeaderId
import com.naminhyeok.fantazzk.room.domain.RoomTeamMemberId
import java.nio.charset.StandardCharsets
import java.util.UUID

fun RoomId(value: Long): RoomId = legacyRoomId(value)

fun RoomId(value: RoomId): RoomId = value

internal fun legacyRoomId(value: Long): RoomId = RoomId.from(stableUuid("room", value))

internal fun stableUuid(
    prefix: String,
    value: Long,
): UUID =
    UUID
        .nameUUIDFromBytes("$prefix:$value".toByteArray(StandardCharsets.UTF_8))

fun roomPlayerId(value: Long): RoomPlayerId = RoomPlayerId.from(stableUuid("room-player", value))

fun roomTeamLeaderId(value: Long): RoomTeamLeaderId = RoomTeamLeaderId.from(stableUuid("room-team-leader", value))

fun roomTeamMemberId(value: Long): RoomTeamMemberId = RoomTeamMemberId.from(stableUuid("room-team-member", value))

fun roomBidId(value: Long): RoomBidId = RoomBidId.from(stableUuid("room-bid", value))

fun <T> nullable(value: T?): T? = value
