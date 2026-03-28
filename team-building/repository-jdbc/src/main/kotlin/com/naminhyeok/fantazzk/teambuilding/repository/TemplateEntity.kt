package com.naminhyeok.fantazzk.teambuilding.repository

import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table

@Table("template")
class TemplateEntity(
    @Column("name") val name: String,
    @Column("mode") val mode: String,
    @Column("rules_json") val rulesJson: String,
    @Column("players_json") val playersJson: String,
) {
    @Id
    var id: Long = 0L
}
