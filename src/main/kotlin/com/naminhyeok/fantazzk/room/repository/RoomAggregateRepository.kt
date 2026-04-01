package com.naminhyeok.fantazzk.room.repository

import com.naminhyeok.fantazzk.room.Room
import com.naminhyeok.fantazzk.room.RoomBid
import com.naminhyeok.fantazzk.room.RoomPlayer
import com.naminhyeok.fantazzk.room.RoomTeamLeader
import com.naminhyeok.fantazzk.room.RoomTeamMember

internal interface RoomAggregateRepository {
    fun findByCode(code: String): Room?

    fun save(room: Room): Room
}

internal class RoomAggregateRepositoryImpl(
    private val roomRepository: RoomRepository,
    private val roomPlayerRepository: RoomPlayerRepository,
    private val roomTeamLeaderRepository: RoomTeamLeaderRepository,
    private val roomTeamMemberRepository: RoomTeamMemberRepository,
    private val roomBidRepository: RoomBidRepository,
) : RoomAggregateRepository {
    override fun findByCode(code: String): Room? {
        val roomModel = roomRepository.findByCode(code) ?: return null
        val room = Room.from(roomModel)
        return room.copy(
            players = roomPlayerRepository.findByRoomId(room.roomId).map(RoomPlayer::from),
            leaders = roomTeamLeaderRepository.findByRoomId(room.roomId).map(RoomTeamLeader::from),
            members = roomTeamMemberRepository.findByRoomId(room.roomId).map(RoomTeamMember::from),
            bids =
                room.currentAuctionRound
                    ?.let { round -> roomBidRepository.findByRoomIdAndRound(room.roomId, round).map(RoomBid::from) }
                    .orEmpty(),
        )
    }

    override fun save(room: Room): Room {
        val pendingEvents = room.pendingEvents()
        val savedRoom = Room.from(roomRepository.save(room))
        val roomToPersist =
            if (room.roomId == 0L && savedRoom.roomId != 0L) {
                room.copy(
                    roomId = savedRoom.roomId,
                    players = room.players.map { it.copy(roomId = savedRoom.roomId) },
                    leaders = room.leaders.map { it.copy(roomId = savedRoom.roomId) },
                    members = room.members.map { it.copy(roomId = savedRoom.roomId) },
                    bids = room.bids.map { it.copy(roomId = savedRoom.roomId) },
                )
            } else {
                room.copy(roomId = savedRoom.roomId)
            }

        val savedPlayers = roomToPersist.players.map { RoomPlayer.from(roomPlayerRepository.save(it)) }
        val savedLeaders = roomToPersist.leaders.map { RoomTeamLeader.from(roomTeamLeaderRepository.save(it)) }
        val savedMembers = roomToPersist.members.map { RoomTeamMember.from(roomTeamMemberRepository.save(it)) }
        val savedBids = roomToPersist.bids.map { RoomBid.from(roomBidRepository.save(it)) }

        return roomToPersist.copy(
            players = savedPlayers,
            leaders = savedLeaders,
            members = savedMembers,
            bids = savedBids,
        ).restorePendingEvents(pendingEvents)
    }
}
