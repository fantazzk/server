package com.naminhyeok.fantazzk.room.repository

import com.naminhyeok.fantazzk.room.DraftOrderStrategy
import com.naminhyeok.fantazzk.room.RoomStatus
import com.naminhyeok.fantazzk.room.TeamBuildingMode
import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table
import java.time.Instant

@Table("room")
class RoomEntity(
    @Column val code: String,
    @Column val hostId: String,
    @Column val status: RoomStatus,
    @Column val mode: TeamBuildingMode,
    @Column val teamCount: Int,
    @Column val teamSize: Int,
    @Column val budget: Int?,
    @Column val draftOrderStrategy: DraftOrderStrategy?,
    @Column val currentTurnIndex: Int?,
    @Column val currentAuctionRound: Int?,
    @Column val createdAt: Instant = Instant.now(),
    @Column val updatedAt: Instant = Instant.now(),
) {
    @Id
    var id: Long = 0L
}
