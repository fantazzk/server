package com.naminhyeok.fantazzk.room.outport

interface TemplateFetcher {
    fun getTemplate(templateId: Long): TemplateSnapshot

    fun getPlayers(templateId: Long): List<TemplatePlayerSnapshot>
}
