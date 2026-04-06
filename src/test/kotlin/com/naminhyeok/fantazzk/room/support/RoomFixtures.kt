package com.naminhyeok.fantazzk.room.support

import com.naminhyeok.fantazzk.room.RoomId
import com.naminhyeok.fantazzk.room.domain.DraftOrderStrategy
import com.naminhyeok.fantazzk.room.domain.PlayerStatus
import com.naminhyeok.fantazzk.room.domain.Room
import com.naminhyeok.fantazzk.room.domain.RoomBid
import com.naminhyeok.fantazzk.room.domain.RoomBidId
import com.naminhyeok.fantazzk.room.domain.RoomPlayer
import com.naminhyeok.fantazzk.room.domain.RoomPlayerId
import com.naminhyeok.fantazzk.room.domain.RoomStatus
import com.naminhyeok.fantazzk.room.domain.RoomTeamLeader
import com.naminhyeok.fantazzk.room.domain.RoomTeamLeaderId
import com.naminhyeok.fantazzk.room.domain.RoomTeamMember
import com.naminhyeok.fantazzk.room.domain.RoomTeamMemberId
import com.naminhyeok.fantazzk.room.domain.TeamBuildingMode
import java.time.Instant

fun roomFixture(
    roomId: RoomId = RoomId.random(),
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
    createdAt: Instant = Instant.now(),
    updatedAt: Instant = Instant.now(),
): Room =
    Room.restore(
        roomId,
        code,
        hostId,
        status,
        mode,
        teamCount,
        teamSize,
        budget,
        draftOrderStrategy,
        currentTurnIndex,
        currentAuctionRound,
        players,
        leaders,
        members,
        bids,
        createdAt,
        updatedAt,
    )

fun copyRoom(
    room: Room,
    roomId: RoomId = room.roomId,
    code: String = room.code,
    hostId: String = room.hostId,
    status: RoomStatus = room.status,
    mode: TeamBuildingMode = room.mode,
    teamCount: Int = room.teamCount,
    teamSize: Int = room.teamSize,
    budget: Int? = room.budget,
    draftOrderStrategy: DraftOrderStrategy? = room.draftOrderStrategy,
    currentTurnIndex: Int? = room.currentTurnIndex,
    currentAuctionRound: Int? = room.currentAuctionRound,
    players: List<RoomPlayer> = room.players.map(RoomPlayer::copy),
    leaders: List<RoomTeamLeader> = room.leaders.map(RoomTeamLeader::copy),
    members: List<RoomTeamMember> = room.members.map(RoomTeamMember::copy),
    bids: List<RoomBid> = room.bidHistory().map(RoomBid::copy),
    createdAt: Instant = room.createdAt,
    updatedAt: Instant = room.updatedAt,
): Room =
    roomFixture(
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

fun playerFixture(
    roomId: RoomId,
    name: String,
    displayOrder: Int,
    roomPlayerId: RoomPlayerId? = null,
    status: PlayerStatus = PlayerStatus.AVAILABLE,
    createdAt: Instant = Instant.now(),
    updatedAt: Instant = Instant.now(),
): RoomPlayer =
    RoomPlayer.restore(
        roomPlayerId,
        roomId,
        name,
        status,
        displayOrder,
        createdAt,
        updatedAt,
    )

fun leaderFixture(
    roomId: RoomId,
    teamLeaderId: String,
    nickname: String,
    roomTeamLeaderId: RoomTeamLeaderId? = null,
    remainingBudget: Int? = null,
    createdAt: Instant = Instant.now(),
    updatedAt: Instant = Instant.now(),
): RoomTeamLeader =
    RoomTeamLeader.restore(
        roomTeamLeaderId,
        roomId,
        teamLeaderId,
        nickname,
        remainingBudget,
        createdAt,
        updatedAt,
    )

fun memberFixture(
    roomId: RoomId,
    teamLeaderId: String,
    playerName: String,
    assignOrder: Int,
    roomTeamMemberId: RoomTeamMemberId? = null,
    createdAt: Instant = Instant.now(),
    updatedAt: Instant = Instant.now(),
): RoomTeamMember =
    RoomTeamMember.restore(
        roomTeamMemberId,
        roomId,
        teamLeaderId,
        playerName,
        assignOrder,
        createdAt,
        updatedAt,
    )

fun bidFixture(
    roomId: RoomId,
    round: Int,
    teamLeaderId: String,
    amount: Int,
    roomBidId: RoomBidId? = null,
    createdAt: Instant = Instant.now(),
    updatedAt: Instant = Instant.now(),
): RoomBid =
    RoomBid.restore(
        roomBidId,
        roomId,
        round,
        teamLeaderId,
        amount,
        createdAt,
        updatedAt,
    )
