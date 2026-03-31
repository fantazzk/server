package com.naminhyeok.fantazzk.room.repository.jdbc

import com.naminhyeok.fantazzk.room.infrastructure.RoomTeamLeaderRepository
import com.naminhyeok.fantazzk.room.model.RoomTeamLeader
import com.naminhyeok.fantazzk.room.model.RoomTeamLeaderModel
import org.springframework.data.repository.CrudRepository

interface RoomTeamLeaderJdbcCrudRepository : CrudRepository<RoomTeamLeaderEntity, Long> {
    fun findByRoomIdOrderById(roomId: Long): List<RoomTeamLeaderEntity>

    fun findByRoomIdAndTeamLeaderId(
        roomId: Long,
        teamLeaderId: String,
    ): RoomTeamLeaderEntity?
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
                createdAt = leader.createdAt,
                updatedAt = leader.updatedAt,
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
            createdAt = createdAt,
            updatedAt = updatedAt,
        )
}
