package com.naminhyeok.fantazzk.teambuilding.room

data class Team(val teamLeader: TeamLeader) {
    val members: List<Player> get() = teamLeader.team
}
