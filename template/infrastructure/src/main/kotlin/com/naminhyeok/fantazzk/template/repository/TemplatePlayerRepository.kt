package com.naminhyeok.fantazzk.template.repository

import com.naminhyeok.fantazzk.template.TemplatePlayer
import com.naminhyeok.fantazzk.template.TemplatePlayerModel

interface TemplatePlayerRepository {
    fun saveAll(players: List<TemplatePlayer>): List<TemplatePlayerModel>
    fun findByTemplateId(templateId: Long): List<TemplatePlayerModel>
}
