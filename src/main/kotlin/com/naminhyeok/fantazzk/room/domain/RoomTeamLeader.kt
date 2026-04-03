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
@Table(name = "room_team_leader")
class RoomTeamLeader protected constructor(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    var roomTeamLeaderId: Long = 0L,
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "room_id", nullable = false)
    private var room: Room? = null,
    @Column(name = "team_leader_id", nullable = false)
    val teamLeaderId: String = "",
    @Column(name = "nickname", nullable = false)
    val nickname: String = "",
    @Column(name = "remaining_budget")
    var remainingBudget: Int? = null,
    @Column(name = "created_at", nullable = false)
    val createdAt: Instant = Instant.now(),
    @Column(name = "updated_at", nullable = false)
    val updatedAt: Instant = Instant.now(),
) {
    constructor(
        roomTeamLeaderId: Long = 0L,
        roomId: Long,
        teamLeaderId: String,
        nickname: String,
        remainingBudget: Int? = null,
        createdAt: Instant = Instant.now(),
        updatedAt: Instant = Instant.now(),
    ) : this(
        roomTeamLeaderId = roomTeamLeaderId,
        room = roomId.takeIf { it != 0L }?.let(Room::reference),
        teamLeaderId = teamLeaderId,
        nickname = nickname,
        remainingBudget = remainingBudget,
        createdAt = createdAt,
        updatedAt = updatedAt,
    )

    constructor(
        roomId: Long = 0L,
        teamLeaderId: String,
        nickname: String,
        remainingBudget: Int? = null,
        createdAt: Instant = Instant.now(),
        updatedAt: Instant = Instant.now(),
    ) : this(
        roomTeamLeaderId = 0L,
        roomId = roomId,
        teamLeaderId = teamLeaderId,
        nickname = nickname,
        remainingBudget = remainingBudget,
        createdAt = createdAt,
        updatedAt = updatedAt,
    )

    val roomId: Long
        get() = room?.roomId ?: 0L

    fun requireCanBid(amount: Int) {
        budgetState().requireCanBid(amount)
    }

    fun spend(amount: Int): RoomTeamLeader =
        apply {
            remainingBudget = budgetState().spend(amount).remainingBudget
        }

    private fun budgetState(): BudgetState = BudgetState.requireFrom(remainingBudget)

    internal fun attach(room: Room) {
        this.room = room
    }

    internal fun detachCopy(): RoomTeamLeader =
        RoomTeamLeader(
            roomTeamLeaderId = roomTeamLeaderId,
            roomId = roomId,
            teamLeaderId = teamLeaderId,
            nickname = nickname,
            remainingBudget = remainingBudget,
            createdAt = createdAt,
            updatedAt = updatedAt,
        )

    fun copy(
        roomTeamLeaderId: Long = this.roomTeamLeaderId,
        roomId: Long = this.roomId,
        teamLeaderId: String = this.teamLeaderId,
        nickname: String = this.nickname,
        remainingBudget: Int? = this.remainingBudget,
        createdAt: Instant = this.createdAt,
        updatedAt: Instant = this.updatedAt,
    ): RoomTeamLeader =
        RoomTeamLeader(
            roomTeamLeaderId = roomTeamLeaderId,
            roomId = roomId,
            teamLeaderId = teamLeaderId,
            nickname = nickname,
            remainingBudget = remainingBudget,
            createdAt = createdAt,
            updatedAt = updatedAt,
        )
}

fun RoomTeamLeader.validateBudget(amount: Int) = requireCanBid(amount)
