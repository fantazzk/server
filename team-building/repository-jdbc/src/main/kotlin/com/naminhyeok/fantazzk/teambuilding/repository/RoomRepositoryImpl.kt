package com.naminhyeok.fantazzk.teambuilding.repository

import com.naminhyeok.fantazzk.teambuilding.room.PlayerPool
import com.naminhyeok.fantazzk.teambuilding.room.Progression
import com.naminhyeok.fantazzk.teambuilding.room.Room
import com.naminhyeok.fantazzk.teambuilding.room.RoomId
import com.naminhyeok.fantazzk.teambuilding.room.RoomResult
import com.naminhyeok.fantazzk.teambuilding.room.RoomSettings
import com.naminhyeok.fantazzk.teambuilding.room.RoomStatus
import com.naminhyeok.fantazzk.teambuilding.room.TeamLeader
import com.naminhyeok.fantazzk.teambuilding.room.TeamLeaderId
import com.naminhyeok.fantazzk.teambuilding.room.TeamLeaders
import tools.jackson.databind.ObjectMapper

internal class RoomRepositoryImpl(
    private val roomJdbcRepository: RoomJdbcRepository,
    private val objectMapper: ObjectMapper,
) : RoomRepository {
    override fun save(room: Room): Room {
        val entity = toEntity(room)
        val saved = roomJdbcRepository.save(entity)
        return toModel(saved)
    }

    override fun findByCode(code: String): Room? = roomJdbcRepository.findByCode(code)?.let(::toModel)

    override fun findById(id: RoomId): Room? = roomJdbcRepository.findById(id.value).orElse(null)?.let(::toModel)

    private fun toEntity(room: Room): RoomEntity {
        val entity =
            RoomEntity(
                code = room.code,
                hostId = room.hostId.value,
                status = room.status.name,
                settingsJson = objectMapper.writeValueAsString(room.settings),
                playerPoolJson = objectMapper.writeValueAsString(room.playerPool),
                teamLeadersJson = objectMapper.writeValueAsString(room.teamLeaders.values),
                progressionJson = room.progression?.let { objectMapper.writeValueAsString(it) },
                resultJson = room.result?.let { objectMapper.writeValueAsString(it) },
            )
        if (room.id.value != 0L) {
            entity.id = room.id.value
        }
        return entity
    }

    private fun toModel(entity: RoomEntity): Room =
        Room(
            id = RoomId(entity.id),
            code = entity.code,
            hostId = TeamLeaderId(entity.hostId),
            status = RoomStatus.valueOf(entity.status),
            settings = objectMapper.readValue(entity.settingsJson, RoomSettings::class.java),
            playerPool = objectMapper.readValue(entity.playerPoolJson, PlayerPool::class.java),
            teamLeaders =
                TeamLeaders(
                    objectMapper.readValue(
                        entity.teamLeadersJson,
                        objectMapper.typeFactory.constructCollectionType(List::class.java, TeamLeader::class.java),
                    ),
                ),
            progression = entity.progressionJson?.let { objectMapper.readValue(it, Progression::class.java) },
            result = entity.resultJson?.let { objectMapper.readValue(it, RoomResult::class.java) },
        )
}
