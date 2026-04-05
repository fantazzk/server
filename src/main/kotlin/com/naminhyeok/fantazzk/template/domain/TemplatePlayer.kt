package com.naminhyeok.fantazzk.template.domain

import com.naminhyeok.fantazzk.template.TemplateId
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
        get() =
            TemplatePlayerId(
                requireNotNull(persistentId) { "TemplatePlayer id는 저장 후에만 사용할 수 있습니다" },
            )

    internal constructor(
        templatePlayerId: Long? = null,
        templateId: Long,
        name: String,
        displayOrder: Int,
        createdAt: Instant = Instant.now(),
        updatedAt: Instant = Instant.now(),
    ) : this(
        templatePlayerId = templatePlayerId?.let(::TemplatePlayerId),
        templateId = TemplateId(templateId),
        name = name,
        displayOrder = displayOrder,
        createdAt = createdAt,
        updatedAt = updatedAt,
    )

    constructor(
        templatePlayerId: TemplatePlayerId? = null,
        templateId: TemplateId,
        name: String,
        displayOrder: Int,
        createdAt: Instant = Instant.now(),
        updatedAt: Instant = Instant.now(),
    ) : this(
        persistentId = templatePlayerId?.value,
        template = Template.reference(templateId.value),
        name = name,
        displayOrder = displayOrder,
        createdAt = createdAt,
        updatedAt = updatedAt,
    )

    internal constructor(
        templateId: Long,
        name: String,
        displayOrder: Int,
        createdAt: Instant = Instant.now(),
        updatedAt: Instant = Instant.now(),
    ) : this(
        templatePlayerId = null,
        templateId = TemplateId(templateId),
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
        get() = template?.persistedIdOrNull()

    internal fun belongsTo(templateId: TemplateId): Boolean = this.templateId == templateId

    internal fun attach(template: Template): TemplatePlayer =
        apply {
            this.template = template
        }
}

data class TemplatePlayerId(
    val value: Long,
) : Identifier
