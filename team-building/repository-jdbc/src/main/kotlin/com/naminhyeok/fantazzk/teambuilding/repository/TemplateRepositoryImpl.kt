package com.naminhyeok.fantazzk.teambuilding.repository

import com.naminhyeok.fantazzk.teambuilding.TeamBuildingMode
import com.naminhyeok.fantazzk.teambuilding.template.PlayerEntry
import com.naminhyeok.fantazzk.teambuilding.template.Rules
import com.naminhyeok.fantazzk.teambuilding.template.Template
import com.naminhyeok.fantazzk.teambuilding.template.TemplateId
import org.springframework.jdbc.core.RowMapper
import org.springframework.jdbc.core.simple.JdbcClient
import org.springframework.jdbc.support.GeneratedKeyHolder
import tools.jackson.databind.ObjectMapper

internal class TemplateRepositoryImpl(
    private val jdbcClient: JdbcClient,
    private val objectMapper: ObjectMapper,
) : TemplateRepository {
    private val rowMapper =
        RowMapper { rs, _ ->
            Template(
                id = TemplateId(rs.getLong("id")),
                name = rs.getString("name"),
                mode = TeamBuildingMode.valueOf(rs.getString("mode")),
                rules = objectMapper.readValue(rs.getString("rules_json"), Rules::class.java),
                players =
                    objectMapper.readValue(
                        rs.getString("players_json"),
                        objectMapper.typeFactory.constructCollectionType(List::class.java, PlayerEntry::class.java),
                    ),
            )
        }

    override fun save(template: Template): Template {
        if (template.id.value == 0L) {
            val keyHolder = GeneratedKeyHolder()
            jdbcClient
                .sql("INSERT INTO template (name, mode, rules_json, players_json) VALUES (:name, :mode, :rulesJson, :playersJson)")
                .param("name", template.name)
                .param("mode", template.mode.name)
                .param("rulesJson", objectMapper.writeValueAsString(template.rules))
                .param("playersJson", objectMapper.writeValueAsString(template.players))
                .update(keyHolder)
            return template.copy(id = TemplateId(keyHolder.key!!.toLong()))
        }
        jdbcClient
            .sql("UPDATE template SET name = :name, mode = :mode, rules_json = :rulesJson, players_json = :playersJson WHERE id = :id")
            .param("id", template.id.value)
            .param("name", template.name)
            .param("mode", template.mode.name)
            .param("rulesJson", objectMapper.writeValueAsString(template.rules))
            .param("playersJson", objectMapper.writeValueAsString(template.players))
            .update()
        return template
    }

    override fun findById(id: TemplateId): Template? =
        jdbcClient
            .sql("SELECT * FROM template WHERE id = :id")
            .param("id", id.value)
            .query(rowMapper)
            .optional()
            .orElse(null)

    override fun findAll(): List<Template> =
        jdbcClient
            .sql("SELECT * FROM template ORDER BY id")
            .query(rowMapper)
            .list()
}
