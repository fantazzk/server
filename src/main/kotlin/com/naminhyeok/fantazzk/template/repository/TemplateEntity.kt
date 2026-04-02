package com.naminhyeok.fantazzk.template.repository

import com.naminhyeok.fantazzk.template.DraftOrderStrategy
import com.naminhyeok.fantazzk.template.TeamBuildingMode
import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table
import java.time.Instant

@Table("template")
class TemplateEntity(
    @Column val name: String,
    @Column val mode: TeamBuildingMode,
    @Column val teamCount: Int,
    @Column val teamSize: Int,
    @Column val budget: Int?,
    @Column val draftOrderStrategy: DraftOrderStrategy?,
    @Column val createdAt: Instant = Instant.now(),
    @Column val updatedAt: Instant = Instant.now(),
) {
    @Id
    var id: Long = 0L
}
