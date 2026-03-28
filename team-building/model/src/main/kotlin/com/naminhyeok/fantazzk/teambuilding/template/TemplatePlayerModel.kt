package com.naminhyeok.fantazzk.teambuilding.template

interface TemplatePlayerIdentity {
    companion object

    val templatePlayerId: Long
}

interface TemplatePlayerProps {
    val templateId: Long
    val name: String
    val displayOrder: Int
}

interface TemplatePlayerModel : TemplatePlayerIdentity, TemplatePlayerProps
