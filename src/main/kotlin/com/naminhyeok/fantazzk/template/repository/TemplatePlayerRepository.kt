package com.naminhyeok.fantazzk.template.repository

import com.naminhyeok.fantazzk.template.TemplatePlayer
import org.jmolecules.ddd.annotation.Repository

@Repository
interface TemplatePlayerRepository {
    fun saveAll(players: List<TemplatePlayer>): List<TemplatePlayer>

    fun findByTemplateId(templateId: Long): List<TemplatePlayer>
}
