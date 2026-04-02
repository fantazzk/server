package com.naminhyeok.fantazzk.template.domain

import com.naminhyeok.fantazzk.template.DraftOrderStrategy
import com.naminhyeok.fantazzk.template.TeamBuildingMode
import com.naminhyeok.fantazzk.template.TemplateCreated
import com.naminhyeok.fantazzk.template.TemplateId
import com.naminhyeok.fantazzk.template.TemplatePlayerCreated
import jakarta.persistence.CascadeType
import jakarta.persistence.Column
import jakarta.persistence.Embedded
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.OneToMany
import jakarta.persistence.OrderBy
import jakarta.persistence.PostLoad
import jakarta.persistence.Table
import jakarta.persistence.Transient
import org.jmolecules.ddd.types.AggregateRoot
import java.time.Instant

@Entity
@Table(name = "template")
class Template protected constructor(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private var persistentId: Long? = null,
    @Column(name = "name", nullable = false)
    val name: String = "",
    @Embedded
    private var persistentConfiguration: TemplateConfiguration = TemplateConfiguration.auction(teamCount = 1, teamSize = 2, budget = 1),
    @OneToMany(mappedBy = "template", cascade = [CascadeType.ALL], orphanRemoval = true)
    @OrderBy("displayOrder ASC")
    private val persistentPlayers: MutableList<TemplatePlayer> = mutableListOf(),
    @Column(name = "created_at", nullable = false)
    val createdAt: Instant = Instant.now(),
    @Column(name = "updated_at", nullable = false)
    val updatedAt: Instant = Instant.now(),
) : AggregateRoot<Template, TemplateId> {
    @Transient
    private val pendingEvents: MutableList<Any> = mutableListOf()

    constructor(
        templateId: Long = 0L,
        name: String,
        templateConfiguration: com.naminhyeok.fantazzk.template.TemplateConfiguration,
        createdAt: Instant = Instant.now(),
        updatedAt: Instant = Instant.now(),
    ) : this(
        persistentId = templateId.takeIf { it > 0 },
        name = name,
        persistentConfiguration =
            when (templateConfiguration) {
                is com.naminhyeok.fantazzk.template.TemplateConfiguration.Auction ->
                    TemplateConfiguration.auction(
                        teamCount = templateConfiguration.teamCount,
                        teamSize = templateConfiguration.teamSize,
                        budget = templateConfiguration.budgetValue,
                    )

                is com.naminhyeok.fantazzk.template.TemplateConfiguration.Draft ->
                    TemplateConfiguration.draft(
                        teamCount = templateConfiguration.teamCount,
                        teamSize = templateConfiguration.teamSize,
                        strategy = templateConfiguration.strategy,
                    )
            },
        createdAt = createdAt,
        updatedAt = updatedAt,
    )

    override fun getId(): TemplateId = TemplateId(requireNotNull(persistentId))

    val templateId: Long
        get() = persistentId ?: 0L

    val mode: TeamBuildingMode
        get() = persistentConfiguration.mode

    val teamCount: Int
        get() = persistentConfiguration.teamCount

    val teamSize: Int
        get() = persistentConfiguration.teamSize

    val budget: Int?
        get() = persistentConfiguration.budget

    val draftOrderStrategy: DraftOrderStrategy?
        get() = persistentConfiguration.draftOrderStrategy

    val configuration: com.naminhyeok.fantazzk.template.TemplateConfiguration
        get() =
            com.naminhyeok.fantazzk.template.TemplateConfiguration.from(
                mode = mode,
                teamCount = teamCount,
                teamSize = teamSize,
                budget = budget,
                draftOrderStrategy = draftOrderStrategy,
            )

    fun players(): List<TemplatePlayer> = persistentPlayers.toList()

    internal fun recordCreated(): Template =
        registerEvent(
            TemplateCreated(
                templateId = templateId,
                name = name,
                mode = mode,
                teamCount = teamCount,
                teamSize = teamSize,
                budget = budget,
                draftOrderStrategy = draftOrderStrategy,
                players = players().map { TemplatePlayerCreated(name = it.name, displayOrder = it.displayOrder) },
            ),
        )

    internal fun drainEvents(): List<Any> = pendingEvents.toList().also { pendingEvents.clear() }

    internal fun assignId(templateId: TemplateId): Template = apply { persistentId = templateId.value }

    @PostLoad
    private fun validateLoadedState() {
        configuration
    }

    companion object {
        fun createAuction(
            name: String,
            teamCount: Int,
            teamSize: Int,
            budget: Int,
            playerNames: List<String>,
        ): Template =
            Template(
                name = name,
                persistentConfiguration = TemplateConfiguration.auction(teamCount = teamCount, teamSize = teamSize, budget = budget),
            ).registerPlayers(playerNames)

        fun createDraft(
            name: String,
            teamCount: Int,
            teamSize: Int,
            strategy: DraftOrderStrategy,
            playerNames: List<String>,
        ): Template =
            Template(
                name = name,
                persistentConfiguration = TemplateConfiguration.draft(teamCount = teamCount, teamSize = teamSize, strategy = strategy),
            ).registerPlayers(playerNames)

        fun reference(templateId: Long): Template = Template(persistentId = templateId)
    }

    private fun registerEvent(event: Any): Template = apply { pendingEvents += event }

    private fun registerPlayers(playerNames: List<String>): Template =
        apply {
            persistentPlayers.clear()
            playerNames
                .sorted()
                .mapIndexed { index, playerName ->
                    TemplatePlayer(
                        name = playerName,
                        displayOrder = index,
                    ).also { it.attach(this) }
                }.forEach(persistentPlayers::add)
        }
}
