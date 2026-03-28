package com.naminhyeok.fantazzk.teambuilding.room

import com.naminhyeok.fantazzk.teambuilding.DraftOrderStrategy
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test

class ProgressionTest {
    @Test
    fun `AuctionProgression tracks highest bid`() {
        val progression =
            Progression.Auction()
                .addBid(Bid(TeamLeaderId("A"), 100))
                .addBid(Bid(TeamLeaderId("B"), 150))

        assertEquals(150, progression.highestBid()?.amount)
        assertEquals(TeamLeaderId("B"), progression.highestBid()?.teamLeaderId)
    }

    @Test
    fun `AuctionProgression highestBid returns null when no bids`() {
        val progression = Progression.Auction()
        assertNull(progression.highestBid())
    }

    @Test
    fun `Draft snake order generates correct pick sequence for 3 teams`() {
        val teamLeaders = listOf(TeamLeaderId("A"), TeamLeaderId("B"), TeamLeaderId("C"))
        val order = Progression.Draft.generatePickOrder(teamLeaders, DraftOrderStrategy.SNAKE, picksPerTeam = 2)

        assertEquals(
            listOf("A", "B", "C", "C", "B", "A"),
            order.map { it.value },
        )
    }

    @Test
    fun `Draft fixed order generates correct pick sequence`() {
        val teamLeaders = listOf(TeamLeaderId("A"), TeamLeaderId("B"), TeamLeaderId("C"))
        val order = Progression.Draft.generatePickOrder(teamLeaders, DraftOrderStrategy.FIXED, picksPerTeam = 2)

        assertEquals(
            listOf("A", "B", "C", "A", "B", "C"),
            order.map { it.value },
        )
    }

    @Test
    fun `Draft currentTurn returns correct team leader`() {
        val draft =
            Progression.Draft(
                pickOrder = listOf(TeamLeaderId("A"), TeamLeaderId("B"), TeamLeaderId("C")),
            )
        assertEquals(TeamLeaderId("A"), draft.currentTurn())
    }

    @Test
    fun `Draft advanceTurn moves to next turn`() {
        val draft =
            Progression.Draft(
                pickOrder = listOf(TeamLeaderId("A"), TeamLeaderId("B"), TeamLeaderId("C")),
            )
        val next = draft.advanceTurn()
        assertEquals(TeamLeaderId("B"), next.currentTurn())
    }
}
