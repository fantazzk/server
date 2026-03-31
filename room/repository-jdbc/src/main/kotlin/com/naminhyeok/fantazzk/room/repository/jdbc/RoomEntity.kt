package com.naminhyeok.fantazzk.room.repository.jdbc

import com.naminhyeok.fantazzk.room.model.DraftOrderStrategy
import com.naminhyeok.fantazzk.room.model.RoomModel
import com.naminhyeok.fantazzk.room.model.RoomStatus
import com.naminhyeok.fantazzk.room.model.TeamBuildingMode
import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table
import java.time.Instant

@Table("room")
class RoomEntity(
    @Column override val code: String,
    @Column override val hostId: String,
    @Column override val status: RoomStatus,
    @Column override val mode: TeamBuildingMode,
    @Column override val teamCount: Int,
    @Column override val teamSize: Int,
    @Column override val budget: Int?,
    @Column override val draftOrderStrategy: DraftOrderStrategy?,
    @Column override val currentTurnIndex: Int?,
    @Column override val currentAuctionRound: Int?,
    @Column override val createdAt: Instant = Instant.now(),
    @Column override val updatedAt: Instant = Instant.now(),
) : RoomModel {
    @Id
    var id: Long = 0L

    override val roomId: Long get() = id
}
