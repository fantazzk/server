package com.naminhyeok.fantazzk.template

import org.jmolecules.ddd.types.AggregateRoot
import org.springframework.data.annotation.Transient
import java.time.Instant

data class Template(
    val templateId: Long = 0L,
    val name: String,
    private val templateConfiguration: TemplateConfiguration,
    val createdAt: Instant = Instant.now(),
    val updatedAt: Instant = Instant.now(),
) : AggregateRoot<Template, TemplateId> {
    @Transient
    private val pendingEvents: MutableList<Any> = mutableListOf()

    override fun getId(): TemplateId = TemplateId(templateId)

    val mode: TeamBuildingMode
        get() = templateConfiguration.mode

    val teamCount: Int
        get() = templateConfiguration.teamCount

    val teamSize: Int
        get() = templateConfiguration.teamSize

    val budget: Int?
        get() = templateConfiguration.budget

    val draftOrderStrategy: DraftOrderStrategy?
        get() = templateConfiguration.draftOrderStrategy

    internal fun recordCreated(players: List<TemplatePlayer>): Template =
        registerEvent(
            TemplateCreated(
                templateId = templateId,
                name = name,
                mode = mode,
                teamCount = teamCount,
                teamSize = teamSize,
                budget = budget,
                draftOrderStrategy = draftOrderStrategy,
                players = players.map { TemplatePlayerCreated(name = it.name, displayOrder = it.displayOrder) },
            ),
        )

    internal fun pendingEvents(): List<Any> = pendingEvents.toList()

    internal fun drainEvents(): List<Any> = pendingEvents.toList().also { pendingEvents.clear() }

    internal fun restorePendingEvents(events: Collection<Any>): Template = apply { pendingEvents.addAll(events) }

    companion object {
        fun create(
            name: String,
            configuration: TemplateConfiguration,
        ): Template = Template(name = name, templateConfiguration = configuration)
    }

    private fun registerEvent(event: Any): Template = apply { pendingEvents += event }
}

val Template.configuration: TemplateConfiguration
    get() = TemplateConfiguration.from(mode, teamCount, teamSize, budget, draftOrderStrategy)

fun Template.requireValidRoster(players: List<TemplatePlayer>) {
    val orderedPlayerNames = players.sortedBy { it.displayOrder }.map { it.name }
    TemplateRoster.exactlyRequired(orderedPlayerNames, configuration.requiredPlayerCount)
}

val Template.picksPerTeam: Int
    get() = teamSize - 1
