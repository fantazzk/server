package com.naminhyeok.fantazzk.room.query

import com.naminhyeok.fantazzk.room.RoomStatus
import org.springframework.jdbc.core.simple.JdbcClient
import org.springframework.stereotype.Component

@Component
internal open class RoomProjectionWriter(
    private val jdbcClient: JdbcClient,
) {
    open fun upsertRoom(
        roomId: Long,
        code: String,
        status: RoomStatus,
    ) {
        jdbcClient.sql(
            """
            insert into room_view (room_id, code, status)
            values (:roomId, :code, :status)
            on conflict (room_id) do update
            set code = excluded.code,
                status = excluded.status
            """.trimIndent(),
        )
            .param("roomId", roomId)
            .param("code", code)
            .param("status", status.name)
            .update()
    }

    open fun upsertLeader(
        roomId: Long,
        teamLeaderId: String,
        nickname: String,
        remainingBudget: Int?,
    ) {
        jdbcClient.sql(
            """
            insert into room_team_leader_view (room_id, team_leader_id, nickname, remaining_budget)
            values (:roomId, :teamLeaderId, :nickname, :remainingBudget)
            on conflict (room_id, team_leader_id) do update
            set nickname = excluded.nickname,
                remaining_budget = excluded.remaining_budget
            """.trimIndent(),
        )
            .param("roomId", roomId)
            .param("teamLeaderId", teamLeaderId)
            .param("nickname", nickname)
            .param("remainingBudget", remainingBudget)
            .update()
    }
}
