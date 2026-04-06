package com.naminhyeok.fantazzk.template.domain

import com.naminhyeok.fantazzk.template.TemplateId
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import org.jmolecules.ddd.types.Identifier
import java.time.Instant
import java.util.UUID
import org.jmolecules.ddd.types.Entity as DomainEntity

@Entity
@Table(name = "template_player")
class TemplatePlayer(
    @Id
    @Column(name = "id", nullable = false, updatable = false)
    override val id: TemplatePlayerId = TemplatePlayerId.newId(),
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
    constructor(
        templatePlayerId: TemplatePlayerId? = null,
        templateId: TemplateId,
        name: String,
        displayOrder: Int,
        createdAt: Instant = Instant.now(),
        updatedAt: Instant = Instant.now(),
    ) : this(
        id = templatePlayerId ?: TemplatePlayerId.newId(),
        template = Template.reference(templateId),
        name = name,
        displayOrder = displayOrder,
        createdAt = createdAt,
        updatedAt = updatedAt,
    )

    constructor(
        templateId: TemplateId,
        name: String,
        displayOrder: Int,
        createdAt: Instant = Instant.now(),
        updatedAt: Instant = Instant.now(),
    ) : this(
        templatePlayerId = null,
        templateId = templateId,
        name = name,
        displayOrder = displayOrder,
        createdAt = createdAt,
        updatedAt = updatedAt,
    )

    val templateId: TemplateId?
        get() = template?.id

    internal fun belongsTo(templateId: TemplateId): Boolean = this.templateId == templateId

    internal fun attach(template: Template): TemplatePlayer =
        apply {
            this.template = template
        }
}

data class TemplatePlayerId(
    val value: UUID,
) : Identifier {
    companion object {
        @JvmStatic
        fun of(value: String): TemplatePlayerId = TemplatePlayerId(UUID.fromString(value))

        @JvmStatic
        fun of(value: UUID): TemplatePlayerId = TemplatePlayerId(value)

        @JvmStatic
        fun newId(): TemplatePlayerId = TemplatePlayerId(UUID.randomUUID())
    }
}
