package com.naminhyeok.fantazzk.room

import com.naminhyeok.fantazzk.room.domain.DraftOrderStrategy
import com.naminhyeok.fantazzk.room.domain.Room
import com.naminhyeok.fantazzk.room.domain.RoomBid
import com.naminhyeok.fantazzk.room.domain.RoomPlayer
import com.naminhyeok.fantazzk.room.domain.RoomStatus
import com.naminhyeok.fantazzk.room.domain.RoomTeamLeader
import com.naminhyeok.fantazzk.room.domain.RoomTeamMember
import com.naminhyeok.fantazzk.room.domain.TeamBuildingMode
import java.time.Instant

fun roomFixture(
    roomId: Long = 0L,
    code: String = "TEST01",
    hostId: String = "host",
    status: RoomStatus = RoomStatus.WAITING,
    mode: TeamBuildingMode = TeamBuildingMode.AUCTION,
    teamCount: Int = 2,
    teamSize: Int = 3,
    budget: Int? = if (mode == TeamBuildingMode.AUCTION) 300 else null,
    draftOrderStrategy: DraftOrderStrategy? = if (mode == TeamBuildingMode.DRAFT) DraftOrderStrategy.SNAKE else null,
    currentTurnIndex: Int? = null,
    currentAuctionRound: Int? = null,
    players: List<RoomPlayer> = emptyList(),
    leaders: List<RoomTeamLeader> = emptyList(),
    members: List<RoomTeamMember> = emptyList(),
    bids: List<RoomBid> = emptyList(),
    createdAt: Instant = Instant.parse("2025-01-01T00:00:00Z"),
    updatedAt: Instant = Instant.parse("2025-01-01T00:00:00Z"),
): Room =
    Room(
        roomId = roomId,
        code = code,
        hostId = hostId,
        status = status,
        mode = mode,
        teamCount = teamCount,
        teamSize = teamSize,
        budget = budget,
        draftOrderStrategy = draftOrderStrategy,
        currentTurnIndex = currentTurnIndex,
        currentAuctionRound = currentAuctionRound,
        players = players,
        leaders = leaders,
        members = members,
        bids = bids,
        createdAt = createdAt,
        updatedAt = updatedAt,
    )
