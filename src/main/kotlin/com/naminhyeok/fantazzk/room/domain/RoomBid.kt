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
@Table(name = "room_bid")
class RoomBid protected constructor(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private var persistentId: Long? = null,
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "room_id", nullable = false)
    private var room: Room? = null,
    @Column(name = "round", nullable = false)
    val round: Int = 0,
    @Column(name = "team_leader_id", nullable = false)
    val teamLeaderId: String = "",
    @Column(name = "amount", nullable = false)
    val amount: Int = 0,
    @Column(name = "created_at", nullable = false)
    val createdAt: Instant = Instant.now(),
    @Column(name = "updated_at", nullable = false)
    val updatedAt: Instant = Instant.now(),
) {
    internal constructor(
        roomBidId: Long? = null,
        roomId: Long?,
        round: Int,
        teamLeaderId: String,
        amount: Int,
        createdAt: Instant = Instant.now(),
        updatedAt: Instant = Instant.now(),
    ) : this(
        roomBidId = roomBidId?.takeIf { it != 0L }?.let(::RoomBidId),
        roomId = roomId?.takeIf { it != 0L }?.let(::RoomId),
        round = round,
        teamLeaderId = teamLeaderId,
        amount = amount,
        createdAt = createdAt,
        updatedAt = updatedAt,
    )

    constructor(
        roomBidId: RoomBidId? = null,
        roomId: RoomId? = null,
        round: Int,
        teamLeaderId: String,
        amount: Int,
        createdAt: Instant = Instant.now(),
        updatedAt: Instant = Instant.now(),
    ) : this(
        persistentId = roomBidId?.value,
        room = roomId?.let { Room.reference(it.value) },
        round = round,
        teamLeaderId = teamLeaderId,
        amount = amount,
        createdAt = createdAt,
        updatedAt = updatedAt,
    )

    internal constructor(
        roomId: Long? = null,
        round: Int,
        teamLeaderId: String,
        amount: Int,
        createdAt: Instant = Instant.now(),
        updatedAt: Instant = Instant.now(),
    ) : this(
        roomBidId = null,
        roomId = roomId,
        round = round,
        teamLeaderId = teamLeaderId,
        amount = amount,
        createdAt = createdAt,
        updatedAt = updatedAt,
    )

    val id: RoomBidId?
        get() = persistentId?.let(::RoomBidId)

    internal val roomBidId: Long
        get() = persistentId ?: 0L

    internal val roomId: Long
        get() = room?.roomId ?: 0L

    internal fun attach(room: Room): RoomBid = apply { this.room = room }

    internal fun detachCopy(): RoomBid =
        RoomBid(
            roomBidId = id,
            roomId = room?.persistedIdOrNull(),
            round = round,
            teamLeaderId = teamLeaderId,
            amount = amount,
            createdAt = createdAt,
            updatedAt = updatedAt,
        )

    internal fun copy(
        roomBidId: Long = this.roomBidId,
        roomId: Long = this.roomId,
        round: Int = this.round,
        teamLeaderId: String = this.teamLeaderId,
        amount: Int = this.amount,
        createdAt: Instant = this.createdAt,
        updatedAt: Instant = this.updatedAt,
    ): RoomBid =
        RoomBid(
            roomBidId = roomBidId.takeIf { it != 0L },
            roomId = roomId,
            round = round,
            teamLeaderId = teamLeaderId,
            amount = amount,
            createdAt = createdAt,
            updatedAt = updatedAt,
        )
}

data class RoomBidId(
    val value: Long,
) : Identifier {
    init {
        require(value > 0L) { "RoomBidId는 0보다 커야 합니다" }
    }
}
