package com.naminhyeok.fantazzk.room.query

import com.naminhyeok.fantazzk.room.RoomStatus
import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table

@Table("room_view")
class RoomViewEntity(
    @Id
    @Column("room_id")
    val roomId: Long,
    @Column("code")
    val code: String,
    @Column("status")
    val status: RoomStatus,
)

@Table("room_team_leader_view")
class TeamLeaderViewEntity(
    @Id
    val id: Long = 0L,
    @Column("room_id")
    val roomId: Long,
    @Column("team_leader_id")
    val teamLeaderId: String,
    @Column("nickname")
    val nickname: String,
    @Column("remaining_budget")
    val remainingBudget: Int?,
)
