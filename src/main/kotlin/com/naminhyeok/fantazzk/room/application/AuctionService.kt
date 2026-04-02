package com.naminhyeok.fantazzk.room.application

import com.naminhyeok.fantazzk.room.AuctionOutcome
import com.naminhyeok.fantazzk.room.AuctionSettled
import com.naminhyeok.fantazzk.room.RoomBid
import com.naminhyeok.fantazzk.room.exception.RoomException
import com.naminhyeok.fantazzk.room.repository.RoomRepository
import org.springframework.context.ApplicationEventPublisher
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

data class AuctionSettleResult(
    val playerName: String,
    val outcome: AuctionOutcome,
)

interface AuctionService {
    fun placeBid(
        code: String,
        teamLeaderId: String,
        amount: Int,
    ): RoomBid

    fun settle(code: String): AuctionSettleResult
}

@org.jmolecules.ddd.annotation.Service
@Service
internal open class AuctionServiceImpl(
    private val roomRepository: RoomRepository,
    private val events: ApplicationEventPublisher,
) : AuctionService {
    @Transactional
    override fun placeBid(
        code: String,
        teamLeaderId: String,
        amount: Int,
    ): RoomBid {
        val room = roomRepository.findByCode(code) ?: throw RoomException.RoomNotFoundException()
        val savedRoom = roomRepository.save(room.placeBid(teamLeaderId, amount))
        return savedRoom.bids.last()
    }

    @Transactional
    override fun settle(code: String): AuctionSettleResult {
        val room = roomRepository.findByCode(code) ?: throw RoomException.RoomNotFoundException()
        val savedRoom = roomRepository.save(room.settleAuction())
        val domainEvents = savedRoom.drainEvents()
        domainEvents.forEach(events::publishEvent)
        val settled =
            domainEvents
                .filterIsInstance<AuctionSettled>()
                .lastOrNull()
                ?: error("AuctionSettled event was not registered")
        return AuctionSettleResult(settled.playerName, settled.outcome)
    }
}
