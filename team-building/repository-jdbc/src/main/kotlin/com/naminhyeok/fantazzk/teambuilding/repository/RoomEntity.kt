package com.naminhyeok.fantazzk.teambuilding.repository

import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table

@Table("room")
class RoomEntity(
    @Column("code") val code: String,
    @Column("host_id") val hostId: String,
    @Column("status") val status: String,
    @Column("settings_json") val settingsJson: String,
    @Column("player_pool_json") val playerPoolJson: String,
    @Column("team_leaders_json") val teamLeadersJson: String,
    @Column("progression_json") val progressionJson: String?,
    @Column("result_json") val resultJson: String?,
) {
    @Id
    var id: Long = 0L
}
