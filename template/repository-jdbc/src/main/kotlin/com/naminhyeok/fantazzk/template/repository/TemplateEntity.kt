package com.naminhyeok.fantazzk.template.repository

import com.naminhyeok.fantazzk.template.DraftOrderStrategy
import com.naminhyeok.fantazzk.template.TeamBuildingMode
import com.naminhyeok.fantazzk.template.TemplateModel
import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table
import java.time.Instant

@Table("template")
class TemplateEntity(
    @Column override val name: String,
    @Column override val mode: TeamBuildingMode,
    @Column override val teamCount: Int,
    @Column override val teamSize: Int,
    @Column override val budget: Int?,
    @Column override val draftOrderStrategy: DraftOrderStrategy?,
    @Column override val createdAt: Instant = Instant.now(),
    @Column override val updatedAt: Instant = Instant.now(),
) : TemplateModel {
    @Id
    var id: Long = 0L

    override val templateId: Long get() = id
}
