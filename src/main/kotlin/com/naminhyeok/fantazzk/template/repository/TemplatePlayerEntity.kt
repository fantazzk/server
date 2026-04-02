package com.naminhyeok.fantazzk.template.repository

import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table
import java.time.Instant

@Table("template_player")
class TemplatePlayerEntity(
    @Column val templateId: Long,
    @Column val name: String,
    @Column val displayOrder: Int,
    @Column val createdAt: Instant = Instant.now(),
    @Column val updatedAt: Instant = Instant.now(),
) {
    @Id
    var id: Long = 0L
}
