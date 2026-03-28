package com.naminhyeok.fantazzk.teambuilding.room

import com.naminhyeok.fantazzk.teambuilding.DraftOrderStrategy
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class ProgressionTest {
    @Test
    fun `경매 진행 시 최고 입찰가를 추적한다`() {
        val progression =
            Progression.Auction()
                .addBid(Bid(TeamLeaderId("A"), 100))
                .addBid(Bid(TeamLeaderId("B"), 150))

        assertThat(progression.highestBid()?.amount).isEqualTo(150)
        assertThat(progression.highestBid()?.teamLeaderId).isEqualTo(TeamLeaderId("B"))
    }

    @Test
    fun `입찰이 없으면 최고 입찰가는 null이다`() {
        val progression = Progression.Auction()

        assertThat(progression.highestBid()).isNull()
    }

    @Test
    fun `스네이크 드래프트는 3팀 기준 올바른 픽 순서를 생성한다`() {
        val teamLeaders = listOf(TeamLeaderId("A"), TeamLeaderId("B"), TeamLeaderId("C"))
        val order = Progression.Draft.generatePickOrder(teamLeaders, DraftOrderStrategy.SNAKE, picksPerTeam = 2)

        assertThat(order.map { it.value }).containsExactly("A", "B", "C", "C", "B", "A")
    }

    @Test
    fun `고정순서 드래프트는 올바른 픽 순서를 생성한다`() {
        val teamLeaders = listOf(TeamLeaderId("A"), TeamLeaderId("B"), TeamLeaderId("C"))
        val order = Progression.Draft.generatePickOrder(teamLeaders, DraftOrderStrategy.FIXED, picksPerTeam = 2)

        assertThat(order.map { it.value }).containsExactly("A", "B", "C", "A", "B", "C")
    }

    @Test
    fun `현재 턴의 팀장을 올바르게 반환한다`() {
        val draft =
            Progression.Draft(
                pickOrder = listOf(TeamLeaderId("A"), TeamLeaderId("B"), TeamLeaderId("C")),
            )

        assertThat(draft.currentTurn()).isEqualTo(TeamLeaderId("A"))
    }

    @Test
    fun `턴을 진행하면 다음 팀장으로 넘어간다`() {
        val draft =
            Progression.Draft(
                pickOrder = listOf(TeamLeaderId("A"), TeamLeaderId("B"), TeamLeaderId("C")),
            )
        val next = draft.advanceTurn()

        assertThat(next.currentTurn()).isEqualTo(TeamLeaderId("B"))
    }
}
