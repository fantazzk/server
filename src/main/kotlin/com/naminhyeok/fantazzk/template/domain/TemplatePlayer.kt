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
import org.jmolecules.ddd.types.Identifier
import java.time.Instant
import org.jmolecules.ddd.types.Entity as DomainEntity

@Entity
@Table(name = "template_player")
class TemplatePlayer(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private var persistentId: Long? = null,
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
) : DomainEntity<Template, TemplatePlayerId> {
    override val id: TemplatePlayerId
        get() = TemplatePlayerId(persistentId ?: 0L)

    val templatePlayerId: Long
        get() = persistentId ?: 0L

    constructor(
        templatePlayerId: Long = 0L,
        templateId: Long,
        name: String,
        displayOrder: Int,
        createdAt: Instant = Instant.now(),
        updatedAt: Instant = Instant.now(),
    ) : this(
        persistentId = templatePlayerId.takeIf { it > 0L },
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
    ) : this(
        templatePlayerId = 0L,
        templateId = templateId,
        name = name,
        displayOrder = displayOrder,
        createdAt = createdAt,
        updatedAt = updatedAt,
    )

    val templateId: Long
        get() = template?.templateId ?: 0L

    internal fun attach(template: Template): TemplatePlayer =
        apply {
            this.template = template
        }
}

data class TemplatePlayerId(
    val value: Long,
) : Identifier
