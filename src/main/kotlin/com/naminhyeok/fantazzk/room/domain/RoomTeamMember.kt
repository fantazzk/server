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
import org.jmolecules.ddd.types.Identifier
import java.time.Instant

@Entity
@Table(name = "room_team_member")
class RoomTeamMember protected constructor(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private var persistentId: Long? = null,
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
    internal constructor(
        roomTeamMemberId: Long? = null,
        roomId: Long?,
        teamLeaderId: String,
        playerName: String,
        assignOrder: Int,
        createdAt: Instant = Instant.now(),
        updatedAt: Instant = Instant.now(),
    ) : this(
        roomTeamMemberId = roomTeamMemberId?.takeIf { it != 0L }?.let(::RoomTeamMemberId),
        roomId = roomId?.takeIf { it != 0L }?.let(::RoomId),
        teamLeaderId = teamLeaderId,
        playerName = playerName,
        assignOrder = assignOrder,
        createdAt = createdAt,
        updatedAt = updatedAt,
    )

    constructor(
        roomTeamMemberId: RoomTeamMemberId? = null,
        roomId: RoomId? = null,
        teamLeaderId: String,
        playerName: String,
        assignOrder: Int,
        createdAt: Instant = Instant.now(),
        updatedAt: Instant = Instant.now(),
    ) : this(
        persistentId = roomTeamMemberId?.value,
        room = roomId?.let { Room.reference(it.value) },
        teamLeaderId = teamLeaderId,
        playerName = playerName,
        assignOrder = assignOrder,
        createdAt = createdAt,
        updatedAt = updatedAt,
    )

    internal constructor(
        roomId: Long? = null,
        teamLeaderId: String,
        playerName: String,
        assignOrder: Int,
        createdAt: Instant = Instant.now(),
        updatedAt: Instant = Instant.now(),
    ) : this(
        roomTeamMemberId = null,
        roomId = roomId,
        teamLeaderId = teamLeaderId,
        playerName = playerName,
        assignOrder = assignOrder,
        createdAt = createdAt,
        updatedAt = updatedAt,
    )

    val id: RoomTeamMemberId?
        get() = persistentId?.let(::RoomTeamMemberId)

    internal val roomTeamMemberId: Long
        get() = persistentId ?: 0L

    internal val roomId: Long
        get() = room?.roomId ?: 0L

    internal fun attach(room: Room): RoomTeamMember = apply { this.room = room }

    internal fun detachCopy(): RoomTeamMember =
        RoomTeamMember(
            roomTeamMemberId = id,
            roomId = room?.persistedIdOrNull(),
            teamLeaderId = teamLeaderId,
            playerName = playerName,
            assignOrder = assignOrder,
            createdAt = createdAt,
            updatedAt = updatedAt,
        )

    internal fun copy(
        roomTeamMemberId: Long = this.roomTeamMemberId,
        roomId: Long = this.roomId,
        teamLeaderId: String = this.teamLeaderId,
        playerName: String = this.playerName,
        assignOrder: Int = this.assignOrder,
        createdAt: Instant = this.createdAt,
        updatedAt: Instant = this.updatedAt,
    ): RoomTeamMember =
        RoomTeamMember(
            roomTeamMemberId = roomTeamMemberId.takeIf { it != 0L },
            roomId = roomId,
            teamLeaderId = teamLeaderId,
            playerName = playerName,
            assignOrder = assignOrder,
            createdAt = createdAt,
            updatedAt = updatedAt,
        )
}

data class RoomTeamMemberId(
    val value: Long,
) : Identifier {
    init {
        require(value > 0L) { "RoomTeamMemberId는 0보다 커야 합니다" }
    }
}
