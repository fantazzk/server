package com.naminhyeok.fantazzk.room.query

import org.springframework.data.repository.CrudRepository

interface RoomViewProjectionRepository {
    fun save(entity: RoomViewEntity): RoomViewEntity

    fun findByCode(code: String): RoomViewEntity?

    fun findAll(): List<RoomViewEntity>
}

interface TeamLeaderViewProjectionRepository {
    fun save(entity: TeamLeaderViewEntity): TeamLeaderViewEntity

    fun findByRoomIdOrderById(roomId: Long): List<TeamLeaderViewEntity>

    fun findByRoomIdAndTeamLeaderId(
        roomId: Long,
        teamLeaderId: String,
    ): TeamLeaderViewEntity?
}

interface RoomViewCrudRepository : CrudRepository<RoomViewEntity, Long>, RoomViewProjectionRepository

interface TeamLeaderViewCrudRepository : CrudRepository<TeamLeaderViewEntity, Long>, TeamLeaderViewProjectionRepository
