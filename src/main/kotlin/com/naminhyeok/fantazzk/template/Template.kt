package com.naminhyeok.fantazzk.template

import org.jmolecules.ddd.types.AggregateRoot
import org.springframework.data.annotation.Transient
import java.time.Instant

data class Template(
    override val templateId: Long = 0L,
    override val name: String,
    private val templateConfiguration: TemplateConfiguration,
    override val createdAt: Instant = Instant.now(),
    override val updatedAt: Instant = Instant.now(),
) : TemplateModel, AggregateRoot<Template, TemplateId> {
    @Transient
    private val pendingEvents: MutableList<Any> = mutableListOf()

    override fun getId(): TemplateId = TemplateId(templateId)

    override val mode: TeamBuildingMode
        get() = templateConfiguration.mode

    override val teamCount: Int
        get() = templateConfiguration.teamCount

    override val teamSize: Int
        get() = templateConfiguration.teamSize

    override val budget: Int?
        get() = templateConfiguration.budget

    override val draftOrderStrategy: DraftOrderStrategy?
        get() = templateConfiguration.draftOrderStrategy

    internal fun recordCreated(players: List<TemplatePlayerModel>): Template =
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

        fun from(model: TemplateModel): Template =
            Template(
                templateId = model.templateId,
                name = model.name,
                templateConfiguration =
                    TemplateConfiguration.from(
                        model.mode,
                        model.teamCount,
                        model.teamSize,
                        model.budget,
                        model.draftOrderStrategy,
                    ),
                createdAt = model.createdAt,
                updatedAt = model.updatedAt,
            )
    }

    private fun registerEvent(event: Any): Template = apply { pendingEvents += event }
}
