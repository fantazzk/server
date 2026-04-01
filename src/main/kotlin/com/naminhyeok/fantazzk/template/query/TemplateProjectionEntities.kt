package com.naminhyeok.fantazzk.template.query

import com.naminhyeok.fantazzk.template.DraftOrderStrategy
import com.naminhyeok.fantazzk.template.TeamBuildingMode
import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table

@Table("template_view")
class TemplateViewEntity(
    @Id
    @Column("template_id")
    val templateId: Long,
    @Column("name")
    val name: String,
    @Column("mode")
    val mode: TeamBuildingMode,
    @Column("team_count")
    val teamCount: Int,
    @Column("team_size")
    val teamSize: Int,
    @Column("budget")
    val budget: Int?,
    @Column("draft_order_strategy")
    val draftOrderStrategy: DraftOrderStrategy?,
)

@Table("template_player_view")
class TemplatePlayerViewEntity(
    @Id
    val id: Long = 0L,
    @Column("template_id")
    val templateId: Long,
    @Column("name")
    val name: String,
    @Column("display_order")
    val displayOrder: Int,
)
