package com.naminhyeok.fantazzk.template.infrastructure

import com.naminhyeok.fantazzk.template.model.TemplatePlayer
import com.naminhyeok.fantazzk.template.model.TemplatePlayerModel

interface TemplatePlayerRepository {
    fun saveAll(players: List<TemplatePlayer>): List<TemplatePlayerModel>

    fun findByTemplateId(templateId: Long): List<TemplatePlayerModel>
}
