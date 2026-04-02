package com.naminhyeok.fantazzk.room

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
@Table(name = "room_bid")
class RoomBid protected constructor(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    var roomBidId: Long = 0L,
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
    constructor(
        roomBidId: Long = 0L,
        roomId: Long,
        round: Int,
        teamLeaderId: String,
        amount: Int,
        createdAt: Instant = Instant.now(),
        updatedAt: Instant = Instant.now(),
    ) : this(
        roomBidId = roomBidId,
        room = roomId.takeIf { it != 0L }?.let(Room::reference),
        round = round,
        teamLeaderId = teamLeaderId,
        amount = amount,
        createdAt = createdAt,
        updatedAt = updatedAt,
    )

    constructor(
        roomId: Long = 0L,
        round: Int,
        teamLeaderId: String,
        amount: Int,
        createdAt: Instant = Instant.now(),
        updatedAt: Instant = Instant.now(),
    ) : this(
        roomBidId = 0L,
        roomId = roomId,
        round = round,
        teamLeaderId = teamLeaderId,
        amount = amount,
        createdAt = createdAt,
        updatedAt = updatedAt,
    )

    val roomId: Long
        get() = room?.roomId ?: 0L

    internal fun attach(room: Room) {
        this.room = room
    }

    internal fun detachCopy(): RoomBid =
        RoomBid(
            roomBidId = roomBidId,
            roomId = roomId,
            round = round,
            teamLeaderId = teamLeaderId,
            amount = amount,
            createdAt = createdAt,
            updatedAt = updatedAt,
        )

    fun copy(
        roomBidId: Long = this.roomBidId,
        roomId: Long = this.roomId,
        round: Int = this.round,
        teamLeaderId: String = this.teamLeaderId,
        amount: Int = this.amount,
        createdAt: Instant = this.createdAt,
        updatedAt: Instant = this.updatedAt,
    ): RoomBid =
        RoomBid(
            roomBidId = roomBidId,
            roomId = roomId,
            round = round,
            teamLeaderId = teamLeaderId,
            amount = amount,
            createdAt = createdAt,
            updatedAt = updatedAt,
        )
}
