package com.naminhyeok.fantazzk.template.domain

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import java.time.Instant

@Entity
@Table(name = "template_player")
class TemplatePlayer(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    var templatePlayerId: Long = 0L,
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "template_id", nullable = false)
    private var template: Template? = null,
    @Column(name = "name", nullable = false)
    val name: String = "",
    @Column(name = "display_order", nullable = false)
    val displayOrder: Int = 0,
    @Column(name = "created_at", nullable = false)
    val createdAt: Instant = Instant.now(),
    @Column(name = "updated_at", nullable = false)
    val updatedAt: Instant = Instant.now(),
) {
    constructor(
        templatePlayerId: Long = 0L,
        templateId: Long,
        name: String,
        displayOrder: Int,
        createdAt: Instant = Instant.now(),
        updatedAt: Instant = Instant.now(),
    ) : this(
        templatePlayerId = templatePlayerId,
        template = Template.reference(templateId),
        name = name,
        displayOrder = displayOrder,
        createdAt = createdAt,
        updatedAt = updatedAt,
    )

    constructor(
        templateId: Long,
        name: String,
        displayOrder: Int,
        createdAt: Instant = Instant.now(),
        updatedAt: Instant = Instant.now(),
    ) : this(templatePlayerId = 0L, templateId = templateId, name = name, displayOrder = displayOrder, createdAt = createdAt, updatedAt = updatedAt)

    val templateId: Long
        get() = template?.templateId ?: 0L

    internal fun attach(template: Template) {
        this.template = template
    }
}
