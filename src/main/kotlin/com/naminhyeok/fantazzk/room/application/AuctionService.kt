package com.naminhyeok.fantazzk.room.application

import com.naminhyeok.fantazzk.room.AuctionOutcome
import com.naminhyeok.fantazzk.room.RoomBid
import com.naminhyeok.fantazzk.room.exception.RoomException
import com.naminhyeok.fantazzk.room.repository.RoomRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

data class AuctionSettleResult(
    val playerName: String,
    val outcome: AuctionOutcome,
)

@org.jmolecules.ddd.annotation.Service
@Service
class AuctionService(
    private val roomRepository: RoomRepository,
) {
    @Transactional
    fun placeBid(
        code: String,
        teamLeaderId: String,
        amount: Int,
    ): RoomBid {
        val room = roomRepository.findByCode(code) ?: throw RoomException.RoomNotFoundException()
        val savedRoom = roomRepository.save(room.placeBid(teamLeaderId, amount))
        return savedRoom.bids.last()
    }

    @Transactional
    fun settle(code: String): AuctionSettleResult {
        val room = roomRepository.findByCode(code) ?: throw RoomException.RoomNotFoundException()
        val playerName =
            requireNotNull(
                room.players
                    .filter { it.status.name == "AVAILABLE" }
                    .minByOrNull { it.displayOrder }
                    ?.name,
            ) { "경매할 선수가 없습니다" }
        val outcome = if (room.bids.isEmpty()) AuctionOutcome.PASSED else AuctionOutcome.SOLD

        roomRepository.save(room.settleAuction())
        return AuctionSettleResult(playerName, outcome)
    }
}
