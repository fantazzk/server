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
@Table(name = "room_team_leader")
class RoomTeamLeader protected constructor(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private var persistentId: Long? = null,
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
    internal constructor(
        roomTeamLeaderId: Long? = null,
        roomId: Long?,
        teamLeaderId: String,
        nickname: String,
        remainingBudget: Int? = null,
        createdAt: Instant = Instant.now(),
        updatedAt: Instant = Instant.now(),
    ) : this(
        roomTeamLeaderId = roomTeamLeaderId?.takeIf { it != 0L }?.let(::RoomTeamLeaderId),
        roomId = roomId?.takeIf { it != 0L }?.let(::RoomId),
        teamLeaderId = teamLeaderId,
        nickname = nickname,
        remainingBudget = remainingBudget,
        createdAt = createdAt,
        updatedAt = updatedAt,
    )

    constructor(
        roomTeamLeaderId: RoomTeamLeaderId? = null,
        roomId: RoomId? = null,
        teamLeaderId: String,
        nickname: String,
        remainingBudget: Int? = null,
        createdAt: Instant = Instant.now(),
        updatedAt: Instant = Instant.now(),
    ) : this(
        persistentId = roomTeamLeaderId?.value,
        room = roomId?.let { Room.reference(it.value) },
        teamLeaderId = teamLeaderId,
        nickname = nickname,
        remainingBudget = remainingBudget,
        createdAt = createdAt,
        updatedAt = updatedAt,
    )

    internal constructor(
        roomId: Long? = null,
        teamLeaderId: String,
        nickname: String,
        remainingBudget: Int? = null,
        createdAt: Instant = Instant.now(),
        updatedAt: Instant = Instant.now(),
    ) : this(
        roomTeamLeaderId = null,
        roomId = roomId,
        teamLeaderId = teamLeaderId,
        nickname = nickname,
        remainingBudget = remainingBudget,
        createdAt = createdAt,
        updatedAt = updatedAt,
    )

    val id: RoomTeamLeaderId?
        get() = persistentId?.let(::RoomTeamLeaderId)

    internal val roomTeamLeaderId: Long
        get() = persistentId ?: 0L

    internal val roomId: Long
        get() = room?.roomId ?: 0L

    fun requireCanBid(amount: Int) {
        budgetState().requireCanBid(amount)
    }

    fun spend(amount: Int): RoomTeamLeader =
        apply {
            remainingBudget = budgetState().spend(amount).remainingBudget
        }

    private fun budgetState(): BudgetState = BudgetState.requireFrom(remainingBudget)

    internal fun attach(room: Room): RoomTeamLeader = apply { this.room = room }

    internal fun detachCopy(): RoomTeamLeader =
        RoomTeamLeader(
            roomTeamLeaderId = id,
            roomId = room?.persistedIdOrNull(),
            teamLeaderId = teamLeaderId,
            nickname = nickname,
            remainingBudget = remainingBudget,
            createdAt = createdAt,
            updatedAt = updatedAt,
        )

    internal fun copy(
        roomTeamLeaderId: Long = this.roomTeamLeaderId,
        roomId: Long = this.roomId,
        teamLeaderId: String = this.teamLeaderId,
        nickname: String = this.nickname,
        remainingBudget: Int? = this.remainingBudget,
        createdAt: Instant = this.createdAt,
        updatedAt: Instant = this.updatedAt,
    ): RoomTeamLeader =
        RoomTeamLeader(
            roomTeamLeaderId = roomTeamLeaderId.takeIf { it != 0L },
            roomId = roomId,
            teamLeaderId = teamLeaderId,
            nickname = nickname,
            remainingBudget = remainingBudget,
            createdAt = createdAt,
            updatedAt = updatedAt,
        )
}

fun RoomTeamLeader.validateBudget(amount: Int) = requireCanBid(amount)

data class RoomTeamLeaderId(
    val value: Long,
) : Identifier
