package com.naminhyeok.fantazzk.template.model

interface TemplateProps {
    val name: String
    val mode: TeamBuildingMode
    val teamCount: Int
    val teamSize: Int
    val budget: Int?
    val draftOrderStrategy: DraftOrderStrategy?
}
