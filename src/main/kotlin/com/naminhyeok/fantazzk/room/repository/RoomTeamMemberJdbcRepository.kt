package com.naminhyeok.fantazzk.room.repository

import com.naminhyeok.fantazzk.room.RoomTeamMember
import org.springframework.data.repository.CrudRepository

interface RoomTeamMemberJdbcCrudRepository : CrudRepository<RoomTeamMemberEntity, Long> {
    fun findByRoomIdOrderByAssignOrder(roomId: Long): List<RoomTeamMemberEntity>

    fun findByRoomIdAndTeamLeaderIdOrderByAssignOrder(
        roomId: Long,
        teamLeaderId: String,
    ): List<RoomTeamMemberEntity>

    fun countByRoomId(roomId: Long): Int

    fun countByRoomIdAndTeamLeaderId(
        roomId: Long,
        teamLeaderId: String,
    ): Int
}

class RoomTeamMemberRepositoryImpl(
    private val roomTeamMemberJdbcCrudRepository: RoomTeamMemberJdbcCrudRepository,
) : RoomTeamMemberRepository {
    override fun save(member: RoomTeamMember): RoomTeamMember {
        val entity =
            RoomTeamMemberEntity(
                roomId = member.roomId,
                teamLeaderId = member.teamLeaderId,
                playerName = member.playerName,
                assignOrder = member.assignOrder,
                createdAt = member.createdAt,
                updatedAt = member.updatedAt,
            )
        if (member.roomTeamMemberId != 0L) entity.id = member.roomTeamMemberId
        return roomTeamMemberJdbcCrudRepository.save(entity).toDomain()
    }

    override fun findByRoomId(roomId: Long): List<RoomTeamMember> =
        roomTeamMemberJdbcCrudRepository.findByRoomIdOrderByAssignOrder(roomId).map { it.toDomain() }

    override fun findByRoomIdAndTeamLeaderId(
        roomId: Long,
        teamLeaderId: String,
    ): List<RoomTeamMember> =
        roomTeamMemberJdbcCrudRepository
            .findByRoomIdAndTeamLeaderIdOrderByAssignOrder(roomId, teamLeaderId)
            .map { it.toDomain() }

    override fun countByRoomId(roomId: Long): Int = roomTeamMemberJdbcCrudRepository.countByRoomId(roomId)

    override fun countByRoomIdAndTeamLeaderId(
        roomId: Long,
        teamLeaderId: String,
    ): Int = roomTeamMemberJdbcCrudRepository.countByRoomIdAndTeamLeaderId(roomId, teamLeaderId)

    private fun RoomTeamMemberEntity.toDomain() =
        RoomTeamMember(
            roomTeamMemberId = id,
            roomId = roomId,
            teamLeaderId = teamLeaderId,
            playerName = playerName,
            assignOrder = assignOrder,
            createdAt = createdAt,
            updatedAt = updatedAt,
        )
}
