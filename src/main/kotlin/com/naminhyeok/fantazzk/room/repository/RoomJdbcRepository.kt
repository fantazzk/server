package com.naminhyeok.fantazzk.room.repository

import com.naminhyeok.fantazzk.room.Room
import com.naminhyeok.fantazzk.room.RoomId
import com.naminhyeok.fantazzk.room.TeamBuildingMode
import org.springframework.data.repository.CrudRepository

interface RoomJdbcCrudRepository : CrudRepository<RoomEntity, Long> {
    fun findByCode(code: String): RoomEntity?
}

class RoomRepositoryImpl(
    private val roomJdbcCrudRepository: RoomJdbcCrudRepository,
    private val roomPlayerRepository: RoomPlayerRepository,
    private val roomTeamLeaderRepository: RoomTeamLeaderRepository,
    private val roomTeamMemberRepository: RoomTeamMemberRepository,
    private val roomBidRepository: RoomBidRepository,
) : RoomRepository {
    override fun save(room: Room): Room {
        val pendingEvents = room.pendingEvents()
        val entity =
            RoomEntity(
                code = room.code,
                hostId = room.hostId,
                status = room.status,
                mode = room.mode,
                teamCount = room.teamCount,
                teamSize = room.teamSize,
                budget = room.budget,
                draftOrderStrategy = room.draftOrderStrategy,
                currentTurnIndex = room.currentTurnIndex,
                currentAuctionRound = room.currentAuctionRound,
                createdAt = room.createdAt,
                updatedAt = room.updatedAt,
            )
        if (room.roomId != 0L) entity.id = room.roomId
        val savedRoom = roomJdbcCrudRepository.save(entity).toDomain()
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

        val savedPlayers = roomPlayerRepository.saveAll(roomToPersist.players)
        val savedLeaders = roomToPersist.leaders.map(roomTeamLeaderRepository::save)
        val savedMembers = roomToPersist.members.map(roomTeamMemberRepository::save)
        val savedBids = roomToPersist.bids.map(roomBidRepository::save)

        return roomToPersist.copy(
            players = savedPlayers,
            leaders = savedLeaders,
            members = savedMembers,
            bids = savedBids,
        ).restorePendingEvents(pendingEvents)
    }

    override fun findByCode(code: String): Room? = roomJdbcCrudRepository.findByCode(code)?.toAggregate()

    override fun findById(roomId: RoomId): Room? = roomJdbcCrudRepository.findById(roomId.value).orElse(null)?.toAggregate()

    private fun RoomEntity.toDomain() =
        Room(
            roomId = id,
            code = code,
            hostId = hostId,
            status = status,
            mode = mode,
            teamCount = teamCount,
            teamSize = teamSize,
            budget = if (mode == TeamBuildingMode.AUCTION) budget else null,
            draftOrderStrategy = if (mode == TeamBuildingMode.DRAFT) draftOrderStrategy else null,
            currentTurnIndex = currentTurnIndex,
            currentAuctionRound = currentAuctionRound,
            createdAt = createdAt,
            updatedAt = updatedAt,
        )

    private fun RoomEntity.toAggregate(): Room {
        val room = toDomain()
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
}
