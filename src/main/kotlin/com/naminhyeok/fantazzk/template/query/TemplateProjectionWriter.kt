package com.naminhyeok.fantazzk.template.query

import com.naminhyeok.fantazzk.template.DraftOrderStrategy
import com.naminhyeok.fantazzk.template.TeamBuildingMode
import org.springframework.jdbc.core.simple.JdbcClient
import org.springframework.stereotype.Component

@Component
internal open class TemplateProjectionWriter(
    private val jdbcClient: JdbcClient,
) {
    open fun upsertTemplate(
        templateId: Long,
        name: String,
        mode: TeamBuildingMode,
        teamCount: Int,
        teamSize: Int,
        budget: Int?,
        draftOrderStrategy: DraftOrderStrategy?,
    ) {
        jdbcClient.sql(
            """
            insert into template_view (template_id, name, mode, team_count, team_size, budget, draft_order_strategy)
            values (:templateId, :name, :mode, :teamCount, :teamSize, :budget, :draftOrderStrategy)
            on conflict (template_id) do update
            set name = excluded.name,
                mode = excluded.mode,
                team_count = excluded.team_count,
                team_size = excluded.team_size,
                budget = excluded.budget,
                draft_order_strategy = excluded.draft_order_strategy
            """.trimIndent(),
        )
            .param("templateId", templateId)
            .param("name", name)
            .param("mode", mode.name)
            .param("teamCount", teamCount)
            .param("teamSize", teamSize)
            .param("budget", budget)
            .param("draftOrderStrategy", draftOrderStrategy?.name)
            .update()
    }

    open fun replacePlayers(
        templateId: Long,
        players: List<TemplatePlayerView>,
    ) {
        jdbcClient.sql("delete from template_player_view where template_id = :templateId")
            .param("templateId", templateId)
            .update()

        players.forEach { player ->
            jdbcClient.sql(
                """
                insert into template_player_view (template_id, name, display_order)
                values (:templateId, :name, :displayOrder)
                """.trimIndent(),
            )
                .param("templateId", templateId)
                .param("name", player.name)
                .param("displayOrder", player.displayOrder)
                .update()
        }
    }
}
