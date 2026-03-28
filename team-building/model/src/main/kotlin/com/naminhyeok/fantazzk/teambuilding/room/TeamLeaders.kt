package com.naminhyeok.fantazzk.teambuilding.room

data class TeamLeaders(val values: List<TeamLeader>) {
    val size: Int get() = values.size

    fun isFull(teamCount: Int): Boolean = values.size >= teamCount

    fun contains(id: TeamLeaderId): Boolean = values.any { it.id == id }

    fun findById(id: TeamLeaderId): TeamLeader = values.first { it.id == id }

    fun add(leader: TeamLeader): TeamLeaders = TeamLeaders(values + leader)

    fun update(
        id: TeamLeaderId,
        transform: (TeamLeader) -> TeamLeader,
    ): TeamLeaders = TeamLeaders(values.map { if (it.id == id) transform(it) else it })

    fun allPickedEnough(picksPerTeam: Int): Boolean = values.all { it.hasPickedEnough(picksPerTeam) }

    fun ids(): List<TeamLeaderId> = values.map { it.id }

    fun toTeams(): List<Team> = values.map { Team(leaderNickname = it.nickname, members = it.team) }
}
