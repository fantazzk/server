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
import org.springframework.jdbc.core.RowMapper
import org.springframework.jdbc.core.simple.JdbcClient
import org.springframework.jdbc.support.GeneratedKeyHolder
import tools.jackson.databind.ObjectMapper

internal class RoomRepositoryImpl(
    private val jdbcClient: JdbcClient,
    private val objectMapper: ObjectMapper,
) : RoomRepository {
    private val rowMapper =
        RowMapper { rs, _ ->
            Room(
                id = RoomId(rs.getLong("id")),
                code = rs.getString("code"),
                hostId = TeamLeaderId(rs.getString("host_id")),
                status = RoomStatus.valueOf(rs.getString("status")),
                settings = objectMapper.readValue(rs.getString("settings_json"), RoomSettings::class.java),
                playerPool = objectMapper.readValue(rs.getString("player_pool_json"), PlayerPool::class.java),
                teamLeaders =
                    TeamLeaders(
                        objectMapper.readValue(
                            rs.getString("team_leaders_json"),
                            objectMapper.typeFactory.constructCollectionType(List::class.java, TeamLeader::class.java),
                        ),
                    ),
                progression = rs.getString("progression_json")?.let { objectMapper.readValue(it, Progression::class.java) },
                result = rs.getString("result_json")?.let { objectMapper.readValue(it, RoomResult::class.java) },
            )
        }

    override fun save(room: Room): Room {
        if (room.id.value == 0L) {
            val keyHolder = GeneratedKeyHolder()
            jdbcClient
                .sql(
                    """
                    INSERT INTO room (code, host_id, status, settings_json, player_pool_json, team_leaders_json, progression_json, result_json)
                    VALUES (:code, :hostId, :status, :settingsJson, :playerPoolJson, :teamLeadersJson, :progressionJson, :resultJson)
                    """.trimIndent(),
                ).param("code", room.code)
                .param("hostId", room.hostId.value)
                .param("status", room.status.name)
                .param("settingsJson", objectMapper.writeValueAsString(room.settings))
                .param("playerPoolJson", objectMapper.writeValueAsString(room.playerPool))
                .param("teamLeadersJson", objectMapper.writeValueAsString(room.teamLeaders.values))
                .param("progressionJson", room.progression?.let { objectMapper.writeValueAsString(it) })
                .param("resultJson", room.result?.let { objectMapper.writeValueAsString(it) })
                .update(keyHolder)
            return room.copy(id = RoomId(keyHolder.key!!.toLong()))
        }
        jdbcClient
            .sql(
                """
                UPDATE room SET status = :status, settings_json = :settingsJson, player_pool_json = :playerPoolJson,
                    team_leaders_json = :teamLeadersJson, progression_json = :progressionJson, result_json = :resultJson
                WHERE id = :id
                """.trimIndent(),
            ).param("id", room.id.value)
            .param("status", room.status.name)
            .param("settingsJson", objectMapper.writeValueAsString(room.settings))
            .param("playerPoolJson", objectMapper.writeValueAsString(room.playerPool))
            .param("teamLeadersJson", objectMapper.writeValueAsString(room.teamLeaders.values))
            .param("progressionJson", room.progression?.let { objectMapper.writeValueAsString(it) })
            .param("resultJson", room.result?.let { objectMapper.writeValueAsString(it) })
            .update()
        return room
    }

    override fun findByCode(code: String): Room? =
        jdbcClient
            .sql("SELECT * FROM room WHERE code = :code")
            .param("code", code)
            .query(rowMapper)
            .optional()
            .orElse(null)

    override fun findById(id: RoomId): Room? =
        jdbcClient
            .sql("SELECT * FROM room WHERE id = :id")
            .param("id", id.value)
            .query(rowMapper)
            .optional()
            .orElse(null)
}
