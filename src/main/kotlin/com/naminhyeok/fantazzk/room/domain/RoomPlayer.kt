package com.naminhyeok.fantazzk.room.domain

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
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
@Table(name = "room_player")
class RoomPlayer protected constructor(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private var persistentId: Long? = null,
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "room_id", nullable = false)
    private var room: Room? = null,
    @Column(name = "name", nullable = false)
    val name: String = "",
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    var status: PlayerStatus = PlayerStatus.AVAILABLE,
    @Column(name = "display_order", nullable = false)
    var displayOrder: Int = 0,
    @Column(name = "created_at", nullable = false)
    val createdAt: Instant = Instant.now(),
    @Column(name = "updated_at", nullable = false)
    val updatedAt: Instant = Instant.now(),
) {
    internal constructor(
        roomPlayerId: Long? = null,
        roomId: Long?,
        name: String,
        status: PlayerStatus = PlayerStatus.AVAILABLE,
        displayOrder: Int,
        createdAt: Instant = Instant.now(),
        updatedAt: Instant = Instant.now(),
    ) : this(
        roomPlayerId = roomPlayerId?.takeIf { it != 0L }?.let(::RoomPlayerId),
        roomId = roomId?.takeIf { it != 0L }?.let(::RoomId),
        name = name,
        status = status,
        displayOrder = displayOrder,
        createdAt = createdAt,
        updatedAt = updatedAt,
    )

    constructor(
        roomPlayerId: RoomPlayerId? = null,
        roomId: RoomId? = null,
        name: String,
        status: PlayerStatus = PlayerStatus.AVAILABLE,
        displayOrder: Int,
        createdAt: Instant = Instant.now(),
        updatedAt: Instant = Instant.now(),
    ) : this(
        persistentId = roomPlayerId?.value,
        room = roomId?.let { Room.reference(it.value) },
        name = name,
        status = status,
        displayOrder = displayOrder,
        createdAt = createdAt,
        updatedAt = updatedAt,
    )

    internal constructor(
        roomId: Long? = null,
        name: String,
        status: PlayerStatus = PlayerStatus.AVAILABLE,
        displayOrder: Int,
        createdAt: Instant = Instant.now(),
        updatedAt: Instant = Instant.now(),
    ) : this(
        roomPlayerId = null,
        roomId = roomId,
        name = name,
        status = status,
        displayOrder = displayOrder,
        createdAt = createdAt,
        updatedAt = updatedAt,
    )

    val id: RoomPlayerId?
        get() = persistentId?.let(::RoomPlayerId)

    internal val roomPlayerId: Long
        get() = persistentId ?: 0L

    internal val roomId: Long
        get() = room?.roomId ?: 0L

    fun assign(): RoomPlayer {
        check(status == PlayerStatus.AVAILABLE) { "선수를 배정할 수 없습니다" }
        status = PlayerStatus.ASSIGNED
        return this
    }

    fun moveToBack(displayOrder: Int): RoomPlayer {
        check(status == PlayerStatus.AVAILABLE) { "선수를 뒤로 보낼 수 없습니다" }
        require(displayOrder >= 0) { "순서는 0 이상이어야 합니다" }
        require(displayOrder > this.displayOrder) { "현재 순서보다 뒤로만 이동할 수 있습니다" }
        this.displayOrder = displayOrder
        return this
    }

    internal fun attach(room: Room): RoomPlayer = apply { this.room = room }

    internal fun detachCopy(): RoomPlayer =
        RoomPlayer(
            roomPlayerId = id,
            roomId = room?.persistedIdOrNull(),
            name = name,
            status = status,
            displayOrder = displayOrder,
            createdAt = createdAt,
            updatedAt = updatedAt,
        )

    internal fun copy(
        roomPlayerId: Long = this.roomPlayerId,
        roomId: Long = this.roomId,
        name: String = this.name,
        status: PlayerStatus = this.status,
        displayOrder: Int = this.displayOrder,
        createdAt: Instant = this.createdAt,
        updatedAt: Instant = this.updatedAt,
    ): RoomPlayer =
        RoomPlayer(
            roomPlayerId = roomPlayerId.takeIf { it != 0L },
            roomId = roomId,
            name = name,
            status = status,
            displayOrder = displayOrder,
            createdAt = createdAt,
            updatedAt = updatedAt,
        )
}

fun RoomPlayer.isAvailable(): Boolean = status == PlayerStatus.AVAILABLE

enum class PlayerStatus {
    AVAILABLE,
    ASSIGNED,
}

data class RoomPlayerId(
    val value: Long,
) : Identifier {
    init {
        require(value > 0L) { "RoomPlayerId는 0보다 커야 합니다" }
    }
}
