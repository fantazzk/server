package com.naminhyeok.fantazzk.template.domain

import com.naminhyeok.fantazzk.template.TemplateId
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
    override val id: TemplateId
        get() = TemplateId(requireNotNull(persistentId))

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

    val configuration: TemplateConfiguration
        get() =
            TemplateConfiguration.from(
                mode = mode,
                teamCount = teamCount,
                teamSize = teamSize,
                budget = budget,
                draftOrderStrategy = draftOrderStrategy,
            )

    val picksPerTeam: Int
        get() = teamSize - 1

    fun players(): List<TemplatePlayer> = persistentPlayers.toList().also(::requireValidRoster)

    fun requireValidRoster(players: List<TemplatePlayer>) {
        persistentId?.let { templateId ->
            require(players.all { it.templateId == templateId }) {
                "선수는 동일한 템플릿에 속해야 합니다"
            }
        }
        val orderedPlayerNames = players.sortedBy { it.displayOrder }.map { it.name }
        TemplateRoster.exactlyRequired(orderedPlayerNames, configuration.requiredPlayerCount)
    }

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
            ).registerPlayers(playerNames).also { it.requireValidRoster(it.players()) }

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
            ).registerPlayers(playerNames).also { it.requireValidRoster(it.players()) }

        fun reference(templateId: Long): Template = Template(persistentId = templateId)
    }

    private fun registerPlayers(playerNames: List<String>): Template =
        apply {
            persistentPlayers.clear()
            playerNames
                .mapIndexed { index, playerName ->
                    TemplatePlayer(
                        name = playerName,
                        displayOrder = index,
                    ).also { it.attach(this) }
                }.forEach(persistentPlayers::add)
        }
}
