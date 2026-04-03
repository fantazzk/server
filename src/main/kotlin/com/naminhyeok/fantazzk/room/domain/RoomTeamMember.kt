package com.naminhyeok.fantazzk.room.domain

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import java.time.Instant

@Entity
@Table(name = "room_team_member")
class RoomTeamMember protected constructor(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    var roomTeamMemberId: Long = 0L,
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "room_id", nullable = false)
    private var room: Room? = null,
    @Column(name = "team_leader_id", nullable = false)
    val teamLeaderId: String = "",
    @Column(name = "player_name", nullable = false)
    val playerName: String = "",
    @Column(name = "assign_order", nullable = false)
    val assignOrder: Int = 0,
    @Column(name = "created_at", nullable = false)
    val createdAt: Instant = Instant.now(),
    @Column(name = "updated_at", nullable = false)
    val updatedAt: Instant = Instant.now(),
) {
    constructor(
        roomTeamMemberId: Long = 0L,
        roomId: Long,
        teamLeaderId: String,
        playerName: String,
        assignOrder: Int,
        createdAt: Instant = Instant.now(),
        updatedAt: Instant = Instant.now(),
    ) : this(
        roomTeamMemberId = roomTeamMemberId,
        room = roomId.takeIf { it != 0L }?.let(Room::reference),
        teamLeaderId = teamLeaderId,
        playerName = playerName,
        assignOrder = assignOrder,
        createdAt = createdAt,
        updatedAt = updatedAt,
    )

    constructor(
        roomId: Long = 0L,
        teamLeaderId: String,
        playerName: String,
        assignOrder: Int,
        createdAt: Instant = Instant.now(),
        updatedAt: Instant = Instant.now(),
    ) : this(
        roomTeamMemberId = 0L,
        roomId = roomId,
        teamLeaderId = teamLeaderId,
        playerName = playerName,
        assignOrder = assignOrder,
        createdAt = createdAt,
        updatedAt = updatedAt,
    )

    val roomId: Long
        get() = room?.roomId ?: 0L

    internal fun attach(room: Room) {
        this.room = room
    }

    internal fun detachCopy(): RoomTeamMember =
        RoomTeamMember(
            roomTeamMemberId = roomTeamMemberId,
            roomId = roomId,
            teamLeaderId = teamLeaderId,
            playerName = playerName,
            assignOrder = assignOrder,
            createdAt = createdAt,
            updatedAt = updatedAt,
        )

    fun copy(
        roomTeamMemberId: Long = this.roomTeamMemberId,
        roomId: Long = this.roomId,
        teamLeaderId: String = this.teamLeaderId,
        playerName: String = this.playerName,
        assignOrder: Int = this.assignOrder,
        createdAt: Instant = this.createdAt,
        updatedAt: Instant = this.updatedAt,
    ): RoomTeamMember =
        RoomTeamMember(
            roomTeamMemberId = roomTeamMemberId,
            roomId = roomId,
            teamLeaderId = teamLeaderId,
            playerName = playerName,
            assignOrder = assignOrder,
            createdAt = createdAt,
            updatedAt = updatedAt,
        )
}
