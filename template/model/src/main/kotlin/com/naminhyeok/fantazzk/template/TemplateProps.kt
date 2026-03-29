package com.naminhyeok.fantazzk.template

interface TemplateProps {
    val name: String
    val mode: TeamBuildingMode
    val teamCount: Int
    val teamSize: Int
    val budget: Int?
    val draftOrderStrategy: DraftOrderStrategy?
}
