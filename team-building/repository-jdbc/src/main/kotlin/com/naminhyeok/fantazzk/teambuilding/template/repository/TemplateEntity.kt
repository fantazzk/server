package com.naminhyeok.fantazzk.teambuilding.template.repository

import com.naminhyeok.fantazzk.teambuilding.DraftOrderStrategy
import com.naminhyeok.fantazzk.teambuilding.TeamBuildingMode
import com.naminhyeok.fantazzk.teambuilding.template.TemplateModel
import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table

@Table("template")
class TemplateEntity(
    @Column override val name: String,
    @Column override val mode: TeamBuildingMode,
    @Column override val teamCount: Int,
    @Column override val teamSize: Int,
    @Column override val budget: Int?,
    @Column override val draftOrderStrategy: DraftOrderStrategy?,
) : TemplateModel {
    @Id
    var id: Long = 0L

    override val templateId: Long get() = id
}
