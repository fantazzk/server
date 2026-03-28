package com.naminhyeok.fantazzk.teambuilding.room.repository

import com.naminhyeok.fantazzk.teambuilding.room.RoomTeamMember
import com.naminhyeok.fantazzk.teambuilding.room.RoomTeamMemberModel
import org.springframework.data.repository.CrudRepository

interface RoomTeamMemberJdbcCrudRepository : CrudRepository<RoomTeamMemberEntity, Long> {
    fun findByRoomId(roomId: Long): List<RoomTeamMemberEntity>

    fun findByRoomIdAndTeamLeaderId(
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
    override fun save(member: RoomTeamMember): RoomTeamMemberModel {
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
        return roomTeamMemberJdbcCrudRepository.save(entity).toModel()
    }

    override fun findByRoomId(roomId: Long): List<RoomTeamMemberModel> =
        roomTeamMemberJdbcCrudRepository.findByRoomId(roomId).map { it.toModel() }

    override fun findByRoomIdAndTeamLeaderId(
        roomId: Long,
        teamLeaderId: String,
    ): List<RoomTeamMemberModel> = roomTeamMemberJdbcCrudRepository.findByRoomIdAndTeamLeaderId(roomId, teamLeaderId).map { it.toModel() }

    override fun countByRoomId(roomId: Long): Int = roomTeamMemberJdbcCrudRepository.countByRoomId(roomId)

    override fun countByRoomIdAndTeamLeaderId(roomId: Long, teamLeaderId: String): Int =
        roomTeamMemberJdbcCrudRepository.countByRoomIdAndTeamLeaderId(roomId, teamLeaderId)

    private fun RoomTeamMemberEntity.toModel() =
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
