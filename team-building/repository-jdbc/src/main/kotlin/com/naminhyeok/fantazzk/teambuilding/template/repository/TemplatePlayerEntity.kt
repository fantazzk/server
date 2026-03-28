package com.naminhyeok.fantazzk.teambuilding.template.repository

import com.naminhyeok.fantazzk.teambuilding.template.TemplatePlayerModel
import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table

@Table("template_player")
class TemplatePlayerEntity(
    @Column override val templateId: Long,
    @Column override val name: String,
    @Column override val displayOrder: Int,
) : TemplatePlayerModel {
    @Id
    var id: Long = 0L

    override val templatePlayerId: Long get() = id
}
