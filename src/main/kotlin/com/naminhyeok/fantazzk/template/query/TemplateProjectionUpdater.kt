package com.naminhyeok.fantazzk.template.query

import com.naminhyeok.fantazzk.template.TemplateCreated
import org.springframework.modulith.events.ApplicationModuleListener
import org.springframework.stereotype.Component

@Component
internal class TemplateProjectionUpdater(
    private val templateProjectionWriter: TemplateProjectionWriter,
) {
    @ApplicationModuleListener
    fun on(event: TemplateCreated) {
        templateProjectionWriter.upsertTemplate(
            templateId = event.templateId,
            name = event.name,
            mode = event.mode,
            teamCount = event.teamCount,
            teamSize = event.teamSize,
            budget = event.budget,
            draftOrderStrategy = event.draftOrderStrategy,
        )

        templateProjectionWriter.replacePlayers(
            templateId = event.templateId,
            players = event.players.map { TemplatePlayerView(name = it.name, displayOrder = it.displayOrder) },
        )
    }
}
