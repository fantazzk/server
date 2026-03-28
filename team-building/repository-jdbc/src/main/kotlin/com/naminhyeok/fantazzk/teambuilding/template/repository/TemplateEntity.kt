package com.naminhyeok.fantazzk.teambuilding.template.repository

import com.naminhyeok.fantazzk.teambuilding.DraftOrderStrategy
import com.naminhyeok.fantazzk.teambuilding.TeamBuildingMode
import com.naminhyeok.fantazzk.teambuilding.template.TemplateModel
import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table

@Table("template")
class TemplateEntity(
    @Column("name") override val name: String,
    @Column("mode") val modeValue: String,
    @Column("team_count") override val teamCount: Int,
    @Column("team_size") override val teamSize: Int,
    @Column("budget") override val budget: Int?,
    @Column("draft_order_strategy") val draftOrderStrategyValue: String?,
) : TemplateModel {
    @Id
    var id: Long = 0L

    override val templateId: Long get() = id
    override val mode: TeamBuildingMode get() = TeamBuildingMode.valueOf(modeValue)
    override val draftOrderStrategy: DraftOrderStrategy? get() = draftOrderStrategyValue?.let { DraftOrderStrategy.valueOf(it) }
}
