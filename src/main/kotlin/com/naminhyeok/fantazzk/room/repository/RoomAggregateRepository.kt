package com.naminhyeok.fantazzk.room.repository

import com.naminhyeok.fantazzk.room.Room
import org.jmolecules.ddd.annotation.Repository

@Repository
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
        val room = roomRepository.findByCode(code) ?: return null
        return room.copy(
            players = roomPlayerRepository.findByRoomId(room.roomId),
            leaders = roomTeamLeaderRepository.findByRoomId(room.roomId),
            members = roomTeamMemberRepository.findByRoomId(room.roomId),
            bids =
                room.currentAuctionRound
                    ?.let { round -> roomBidRepository.findByRoomIdAndRound(room.roomId, round) }
                    .orEmpty(),
        )
    }

    override fun save(room: Room): Room {
        val pendingEvents = room.pendingEvents()
        val savedRoom = roomRepository.save(room)
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

        val savedPlayers = roomToPersist.players.map { roomPlayerRepository.save(it) }
        val savedLeaders = roomToPersist.leaders.map { roomTeamLeaderRepository.save(it) }
        val savedMembers = roomToPersist.members.map { roomTeamMemberRepository.save(it) }
        val savedBids = roomToPersist.bids.map { roomBidRepository.save(it) }

        return roomToPersist.copy(
            players = savedPlayers,
            leaders = savedLeaders,
            members = savedMembers,
            bids = savedBids,
        ).restorePendingEvents(pendingEvents)
    }
}
