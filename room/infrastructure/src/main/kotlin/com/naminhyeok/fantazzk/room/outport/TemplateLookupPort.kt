package com.naminhyeok.fantazzk.room.outport

interface TemplateLookupPort {
    fun getTemplate(templateId: Long): TemplateSnapshot

    fun getPlayers(templateId: Long): List<TemplatePlayerSnapshot>
}
