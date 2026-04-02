package com.naminhyeok.fantazzk.room.repository

import com.naminhyeok.fantazzk.room.RoomTeamLeader
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
    override fun save(leader: RoomTeamLeader): RoomTeamLeader {
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
        return roomTeamLeaderJdbcCrudRepository.save(entity).toDomain()
    }

    override fun findByRoomId(roomId: Long): List<RoomTeamLeader> =
        roomTeamLeaderJdbcCrudRepository.findByRoomIdOrderById(roomId).map { it.toDomain() }

    override fun findByRoomIdAndTeamLeaderId(
        roomId: Long,
        teamLeaderId: String,
    ): RoomTeamLeader? = roomTeamLeaderJdbcCrudRepository.findByRoomIdAndTeamLeaderId(roomId, teamLeaderId)?.toDomain()

    private fun RoomTeamLeaderEntity.toDomain() =
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
