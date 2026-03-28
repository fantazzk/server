package com.naminhyeok.fantazzk.teambuilding.room.repository

import com.naminhyeok.fantazzk.teambuilding.room.PlayerStatus
import com.naminhyeok.fantazzk.teambuilding.room.Room
import com.naminhyeok.fantazzk.teambuilding.room.RoomBid
import com.naminhyeok.fantazzk.teambuilding.room.RoomBidModel
import com.naminhyeok.fantazzk.teambuilding.room.RoomModel
import com.naminhyeok.fantazzk.teambuilding.room.RoomPlayer
import com.naminhyeok.fantazzk.teambuilding.room.RoomPlayerModel
import com.naminhyeok.fantazzk.teambuilding.room.RoomStatus
import com.naminhyeok.fantazzk.teambuilding.room.RoomTeamLeader
import com.naminhyeok.fantazzk.teambuilding.room.RoomTeamLeaderModel
import com.naminhyeok.fantazzk.teambuilding.room.RoomTeamMember
import com.naminhyeok.fantazzk.teambuilding.room.RoomTeamMemberModel
import org.springframework.data.repository.CrudRepository
import org.springframework.jdbc.core.simple.JdbcClient

interface RoomJdbcCrudRepository : CrudRepository<RoomEntity, Long> {
    fun findByCode(code: String): RoomEntity?
}

interface RoomPlayerJdbcCrudRepository : CrudRepository<RoomPlayerEntity, Long> {
    fun findByRoomIdOrderByDisplayOrder(roomId: Long): List<RoomPlayerEntity>
}

interface RoomTeamLeaderJdbcCrudRepository : CrudRepository<RoomTeamLeaderEntity, Long> {
    fun findByRoomId(roomId: Long): List<RoomTeamLeaderEntity>

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
}

// --- RepositoryImpl adapters ---

class RoomRepositoryImpl(
    private val roomJdbcCrudRepository: RoomJdbcCrudRepository,
    private val jdbcClient: JdbcClient,
) : RoomRepository {
    override fun save(room: Room): RoomModel {
        val entity =
            RoomEntity(
                code = room.code,
                hostId = room.hostId,
                statusValue = room.status.name,
                modeValue = room.mode.name,
                teamCount = room.teamCount,
                teamSize = room.teamSize,
                budget = room.budget,
                draftOrderStrategyValue = room.draftOrderStrategy?.name,
                currentTurnIndex = room.currentTurnIndex,
                currentAuctionRound = room.currentAuctionRound,
            )
        if (room.roomId != 0L) entity.id = room.roomId
        return roomJdbcCrudRepository.save(entity)
    }

    override fun findByCode(code: String): RoomModel? = roomJdbcCrudRepository.findByCode(code)

    override fun updateStatus(
        roomId: Long,
        status: RoomStatus,
    ) {
        jdbcClient.sql("UPDATE room SET status = :status WHERE id = :id")
            .param("status", status.name)
            .param("id", roomId)
            .update()
    }

    override fun updateCurrentTurnIndex(
        roomId: Long,
        currentTurnIndex: Int,
    ) {
        jdbcClient.sql("UPDATE room SET current_turn_index = :idx WHERE id = :id")
            .param("idx", currentTurnIndex)
            .param("id", roomId)
            .update()
    }

    override fun updateCurrentAuctionRound(
        roomId: Long,
        currentAuctionRound: Int,
    ) {
        jdbcClient.sql("UPDATE room SET current_auction_round = :round WHERE id = :id")
            .param("round", currentAuctionRound)
            .param("id", roomId)
            .update()
    }
}

class RoomPlayerRepositoryImpl(
    private val roomPlayerJdbcCrudRepository: RoomPlayerJdbcCrudRepository,
    private val jdbcClient: JdbcClient,
) : RoomPlayerRepository {
    override fun saveAll(players: List<RoomPlayer>): List<RoomPlayerModel> {
        val entities =
            players.map {
                val entity =
                    RoomPlayerEntity(roomId = it.roomId, name = it.name, statusValue = it.status.name, displayOrder = it.displayOrder)
                if (it.roomPlayerId != 0L) entity.id = it.roomPlayerId
                entity
            }
        return roomPlayerJdbcCrudRepository.saveAll(entities).toList()
    }

    override fun findByRoomId(roomId: Long): List<RoomPlayerModel> = roomPlayerJdbcCrudRepository.findByRoomIdOrderByDisplayOrder(roomId)

    override fun findFirstAvailable(roomId: Long): RoomPlayerModel? =
        roomPlayerJdbcCrudRepository.findByRoomIdOrderByDisplayOrder(roomId)
            .firstOrNull { it.status == PlayerStatus.AVAILABLE }

    override fun updateStatus(
        roomPlayerId: Long,
        status: PlayerStatus,
    ) {
        jdbcClient.sql("UPDATE room_player SET status = :status WHERE id = :id")
            .param("status", status.name)
            .param("id", roomPlayerId)
            .update()
    }

    override fun moveToBack(
        roomId: Long,
        roomPlayerId: Long,
    ) {
        jdbcClient.sql(
            """
            UPDATE room_player
            SET display_order = (
                SELECT COALESCE(MAX(display_order), 0) + 1
                FROM room_player rp WHERE rp.room_id = :roomId
            )
            WHERE id = :id
            """.trimIndent(),
        )
            .param("roomId", roomId)
            .param("id", roomPlayerId)
            .update()
    }
}

class RoomTeamLeaderRepositoryImpl(
    private val roomTeamLeaderJdbcCrudRepository: RoomTeamLeaderJdbcCrudRepository,
    private val jdbcClient: JdbcClient,
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
        return roomTeamLeaderJdbcCrudRepository.save(entity)
    }

    override fun findByRoomId(roomId: Long): List<RoomTeamLeaderModel> = roomTeamLeaderJdbcCrudRepository.findByRoomId(roomId)

    override fun findByRoomIdAndTeamLeaderId(
        roomId: Long,
        teamLeaderId: String,
    ): RoomTeamLeaderModel? = roomTeamLeaderJdbcCrudRepository.findByRoomIdAndTeamLeaderId(roomId, teamLeaderId)

    override fun updateRemainingBudget(
        roomTeamLeaderId: Long,
        remainingBudget: Int,
    ) {
        jdbcClient.sql("UPDATE room_team_leader SET remaining_budget = :budget WHERE id = :id")
            .param("budget", remainingBudget)
            .param("id", roomTeamLeaderId)
            .update()
    }
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
        return roomTeamMemberJdbcCrudRepository.save(entity)
    }

    override fun findByRoomId(roomId: Long): List<RoomTeamMemberModel> = roomTeamMemberJdbcCrudRepository.findByRoomId(roomId)

    override fun findByRoomIdAndTeamLeaderId(
        roomId: Long,
        teamLeaderId: String,
    ): List<RoomTeamMemberModel> = roomTeamMemberJdbcCrudRepository.findByRoomIdAndTeamLeaderId(roomId, teamLeaderId)

    override fun countByRoomId(roomId: Long): Int = roomTeamMemberJdbcCrudRepository.countByRoomId(roomId)
}

class RoomBidRepositoryImpl(
    private val roomBidJdbcCrudRepository: RoomBidJdbcCrudRepository,
) : RoomBidRepository {
    override fun save(bid: RoomBid): RoomBidModel {
        val entity = RoomBidEntity(roomId = bid.roomId, round = bid.round, teamLeaderId = bid.teamLeaderId, amount = bid.amount)
        if (bid.roomBidId != 0L) entity.id = bid.roomBidId
        return roomBidJdbcCrudRepository.save(entity)
    }

    override fun findByRoomIdAndRound(
        roomId: Long,
        round: Int,
    ): List<RoomBidModel> = roomBidJdbcCrudRepository.findByRoomIdAndRound(roomId, round)

    override fun findHighestByRoomIdAndRound(
        roomId: Long,
        round: Int,
    ): RoomBidModel? = roomBidJdbcCrudRepository.findByRoomIdAndRound(roomId, round).maxByOrNull { it.amount }
}
