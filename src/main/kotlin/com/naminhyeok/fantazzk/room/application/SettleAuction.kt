package com.naminhyeok.fantazzk.room.application

import com.naminhyeok.fantazzk.room.domain.AuctionOutcome
import com.naminhyeok.fantazzk.room.exception.RoomException
import com.naminhyeok.fantazzk.room.repository.Rooms
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional

data class AuctionSettleResult(
    val playerName: String,
    val outcome: AuctionOutcome,
)

@Component
class SettleAuction(
    private val rooms: Rooms,
) {
    @Transactional
    fun settle(code: String): AuctionSettleResult {
        val room = rooms.findByCode(code) ?: throw RoomException.RoomNotFoundException()
        val playerName =
            requireNotNull(
                room.players
                    .filter { it.status.name == "AVAILABLE" }
                    .minByOrNull { it.displayOrder }
                    ?.name,
            ) { "경매할 선수가 없습니다" }
        val outcome = if (room.bids.isEmpty()) AuctionOutcome.PASSED else AuctionOutcome.SOLD

        rooms.save(room.settleAuction())
        return AuctionSettleResult(playerName, outcome)
    }
}
