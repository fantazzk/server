package com.naminhyeok.fantazzk.template.model

import java.time.Instant

data class Template(
    override val templateId: Long = 0L,
    override val name: String,
    private val templateConfiguration: TemplateConfiguration,
    override val createdAt: Instant = Instant.now(),
    override val updatedAt: Instant = Instant.now(),
) : TemplateModel {
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

    companion object {
        fun create(
            name: String,
            configuration: TemplateConfiguration,
        ): Template = Template(name = name, templateConfiguration = configuration)
    }
}
