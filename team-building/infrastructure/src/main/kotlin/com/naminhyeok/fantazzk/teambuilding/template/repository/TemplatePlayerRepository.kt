package com.naminhyeok.fantazzk.teambuilding.template.repository

import com.naminhyeok.fantazzk.teambuilding.template.TemplatePlayer
import com.naminhyeok.fantazzk.teambuilding.template.TemplatePlayerModel

interface TemplatePlayerRepository {
    fun saveAll(players: List<TemplatePlayer>): List<TemplatePlayerModel>

    fun findByTemplateId(templateId: Long): List<TemplatePlayerModel>
}
