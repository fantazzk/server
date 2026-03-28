package com.naminhyeok.fantazzk.teambuilding.room.repository

import com.naminhyeok.fantazzk.teambuilding.room.PlayerStatus
import com.naminhyeok.fantazzk.teambuilding.room.Room
import com.naminhyeok.fantazzk.teambuilding.room.RoomBid
import com.naminhyeok.fantazzk.teambuilding.room.RoomBidModel
import com.naminhyeok.fantazzk.teambuilding.room.RoomModel
import com.naminhyeok.fantazzk.teambuilding.room.RoomPlayer
import com.naminhyeok.fantazzk.teambuilding.room.RoomPlayerModel
import com.naminhyeok.fantazzk.teambuilding.room.RoomTeamLeader
import com.naminhyeok.fantazzk.teambuilding.room.RoomTeamLeaderModel
import com.naminhyeok.fantazzk.teambuilding.room.RoomTeamMember
import com.naminhyeok.fantazzk.teambuilding.room.RoomTeamMemberModel
import org.springframework.data.repository.CrudRepository

interface RoomJdbcCrudRepository : CrudRepository<RoomEntity, Long> {
    fun findByCode(code: String): RoomEntity?
}

interface RoomPlayerJdbcCrudRepository : CrudRepository<RoomPlayerEntity, Long> {
    fun findByRoomIdOrderByDisplayOrder(roomId: Long): List<RoomPlayerEntity>

    fun findFirstByRoomIdAndStatusOrderByDisplayOrder(
        roomId: Long,
        status: PlayerStatus,
    ): RoomPlayerEntity?
}

interface RoomTeamLeaderJdbcCrudRepository : CrudRepository<RoomTeamLeaderEntity, Long> {
    fun findByRoomId(roomId: Long): List<RoomTeamLeaderEntity>

    fun findByRoomIdOrderById(roomId: Long): List<RoomTeamLeaderEntity>

    fun findByRoomIdAndTeamLeaderId(
        roomId: Long,
        teamLeaderId: String,
    ): RoomTeamLeaderEntity?
}

interface RoomTeamMemberJdbcCrudRepository : CrudRepository<RoomTeamMemberEntity, Long> {
    fun findByRoomId(roomId: Long): List<RoomTeamMemberEntity>

    fun findByRoomIdAndTeamLeaderId(
        roomId: Long,
        teamLeaderId: String,
    ): List<RoomTeamMemberEntity>

    fun countByRoomId(roomId: Long): Int
}

interface RoomBidJdbcCrudRepository : CrudRepository<RoomBidEntity, Long> {
    fun findByRoomIdAndRound(
        roomId: Long,
        round: Int,
    ): List<RoomBidEntity>

    fun findFirstByRoomIdAndRoundOrderByAmountDesc(
        roomId: Long,
        round: Int,
    ): RoomBidEntity?
}

class RoomRepositoryImpl(
    private val roomJdbcCrudRepository: RoomJdbcCrudRepository,
) : RoomRepository {
    override fun save(room: Room): RoomModel {
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
            )
        if (room.roomId != 0L) entity.id = room.roomId
        return roomJdbcCrudRepository.save(entity).toModel()
    }

    override fun findByCode(code: String): RoomModel? = roomJdbcCrudRepository.findByCode(code)?.toModel()

    override fun findById(roomId: Long): RoomModel? = roomJdbcCrudRepository.findById(roomId).orElse(null)?.toModel()

    private fun RoomEntity.toModel() =
        Room(
            roomId = id,
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
        )
}

class RoomPlayerRepositoryImpl(
    private val roomPlayerJdbcCrudRepository: RoomPlayerJdbcCrudRepository,
) : RoomPlayerRepository {
    override fun save(player: RoomPlayer): RoomPlayerModel {
        val entity =
            RoomPlayerEntity(
                roomId = player.roomId,
                name = player.name,
                status = player.status,
                displayOrder = player.displayOrder,
            )
        if (player.roomPlayerId != 0L) entity.id = player.roomPlayerId
        return roomPlayerJdbcCrudRepository.save(entity).toModel()
    }

    override fun saveAll(players: List<RoomPlayer>): List<RoomPlayerModel> {
        val entities =
            players.map {
                val entity =
                    RoomPlayerEntity(
                        roomId = it.roomId,
                        name = it.name,
                        status = it.status,
                        displayOrder = it.displayOrder,
                    )
                if (it.roomPlayerId != 0L) entity.id = it.roomPlayerId
                entity
            }
        return roomPlayerJdbcCrudRepository.saveAll(entities).map { it.toModel() }
    }

    override fun findByRoomId(roomId: Long): List<RoomPlayerModel> =
        roomPlayerJdbcCrudRepository.findByRoomIdOrderByDisplayOrder(roomId).map { it.toModel() }

    override fun findFirstAvailable(roomId: Long): RoomPlayerModel? =
        roomPlayerJdbcCrudRepository
            .findFirstByRoomIdAndStatusOrderByDisplayOrder(roomId, PlayerStatus.AVAILABLE)
            ?.toModel()

    private fun RoomPlayerEntity.toModel() =
        RoomPlayer(
            roomPlayerId = id,
            roomId = roomId,
            name = name,
            status = status,
            displayOrder = displayOrder,
        )
}

class RoomTeamLeaderRepositoryImpl(
    private val roomTeamLeaderJdbcCrudRepository: RoomTeamLeaderJdbcCrudRepository,
) : RoomTeamLeaderRepository {
    override fun save(leader: RoomTeamLeader): RoomTeamLeaderModel {
        val entity =
            RoomTeamLeaderEntity(
                roomId = leader.roomId,
                teamLeaderId = leader.teamLeaderId,
                nickname = leader.nickname,
                remainingBudget = leader.remainingBudget,
            )
        if (leader.roomTeamLeaderId != 0L) entity.id = leader.roomTeamLeaderId
        return roomTeamLeaderJdbcCrudRepository.save(entity).toModel()
    }

    override fun findByRoomId(roomId: Long): List<RoomTeamLeaderModel> =
        roomTeamLeaderJdbcCrudRepository.findByRoomIdOrderById(roomId).map { it.toModel() }

    override fun findByRoomIdAndTeamLeaderId(
        roomId: Long,
        teamLeaderId: String,
    ): RoomTeamLeaderModel? = roomTeamLeaderJdbcCrudRepository.findByRoomIdAndTeamLeaderId(roomId, teamLeaderId)?.toModel()

    private fun RoomTeamLeaderEntity.toModel() =
        RoomTeamLeader(
            roomTeamLeaderId = id,
            roomId = roomId,
            teamLeaderId = teamLeaderId,
            nickname = nickname,
            remainingBudget = remainingBudget,
        )
}

class RoomTeamMemberRepositoryImpl(
    private val roomTeamMemberJdbcCrudRepository: RoomTeamMemberJdbcCrudRepository,
) : RoomTeamMemberRepository {
    override fun save(member: RoomTeamMember): RoomTeamMemberModel {
        val entity =
            RoomTeamMemberEntity(
                roomId = member.roomId,
                teamLeaderId = member.teamLeaderId,
                playerName = member.playerName,
                assignOrder = member.assignOrder,
            )
        if (member.roomTeamMemberId != 0L) entity.id = member.roomTeamMemberId
        return roomTeamMemberJdbcCrudRepository.save(entity).toModel()
    }

    override fun findByRoomId(roomId: Long): List<RoomTeamMemberModel> =
        roomTeamMemberJdbcCrudRepository.findByRoomId(roomId).map { it.toModel() }

    override fun findByRoomIdAndTeamLeaderId(
        roomId: Long,
        teamLeaderId: String,
    ): List<RoomTeamMemberModel> = roomTeamMemberJdbcCrudRepository.findByRoomIdAndTeamLeaderId(roomId, teamLeaderId).map { it.toModel() }

    override fun countByRoomId(roomId: Long): Int = roomTeamMemberJdbcCrudRepository.countByRoomId(roomId)

    private fun RoomTeamMemberEntity.toModel() =
        RoomTeamMember(
            roomTeamMemberId = id,
            roomId = roomId,
            teamLeaderId = teamLeaderId,
            playerName = playerName,
            assignOrder = assignOrder,
        )
}

class RoomBidRepositoryImpl(
    private val roomBidJdbcCrudRepository: RoomBidJdbcCrudRepository,
) : RoomBidRepository {
    override fun save(bid: RoomBid): RoomBidModel {
        val entity =
            RoomBidEntity(
                roomId = bid.roomId,
                round = bid.round,
                teamLeaderId = bid.teamLeaderId,
                amount = bid.amount,
            )
        if (bid.roomBidId != 0L) entity.id = bid.roomBidId
        return roomBidJdbcCrudRepository.save(entity).toModel()
    }

    override fun findByRoomIdAndRound(
        roomId: Long,
        round: Int,
    ): List<RoomBidModel> = roomBidJdbcCrudRepository.findByRoomIdAndRound(roomId, round).map { it.toModel() }

    override fun findHighestByRoomIdAndRound(
        roomId: Long,
        round: Int,
    ): RoomBidModel? = roomBidJdbcCrudRepository.findFirstByRoomIdAndRoundOrderByAmountDesc(roomId, round)?.toModel()

    private fun RoomBidEntity.toModel() =
        RoomBid(
            roomBidId = id,
            roomId = roomId,
            round = round,
            teamLeaderId = teamLeaderId,
            amount = amount,
        )
}
