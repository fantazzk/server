package com.naminhyeok.fantazzk.room

import com.naminhyeok.fantazzk.room.outport.TemplatePlayerSnapshot
import com.naminhyeok.fantazzk.room.outport.TemplateSnapshot
import com.naminhyeok.fantazzk.room.support.InMemoryRoomPlayerRepository
import com.naminhyeok.fantazzk.room.support.InMemoryRoomRepository
import com.naminhyeok.fantazzk.room.support.InMemoryRoomTeamLeaderRepository
import com.naminhyeok.fantazzk.room.support.InMemoryTemplateFetcher
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.RepeatedTest
import org.junit.jupiter.api.Test

class RoomCreateServiceTest {
    private lateinit var roomRepo: InMemoryRoomRepository
    private lateinit var playerRepo: InMemoryRoomPlayerRepository
    private lateinit var leaderRepo: InMemoryRoomTeamLeaderRepository
    private lateinit var templateFetcher: InMemoryTemplateFetcher
    private lateinit var cut: RoomCreateService

    @BeforeEach
    fun setUp() {
        roomRepo = InMemoryRoomRepository()
        playerRepo = InMemoryRoomPlayerRepository()
        leaderRepo = InMemoryRoomTeamLeaderRepository()
        templateFetcher = InMemoryTemplateFetcher()
        cut = RoomCreateServiceImpl(roomRepo, playerRepo, leaderRepo, templateFetcher)
    }

    @Test
    fun `경매 템플릿으로 방을 생성하면 예산이 설정된다`() {
        templateFetcher.addTemplate(
            1L,
            TemplateSnapshot(mode = TeamBuildingMode.AUCTION, teamCount = 2, teamSize = 2, budget = 300, draftOrderStrategy = null),
            listOf(TemplatePlayerSnapshot("선수1", 0), TemplatePlayerSnapshot("선수2", 1)),
        )

        val room = cut.create(1L, "호스트")

        assertThat(room.status).isEqualTo(RoomStatus.WAITING)
        assertThat(room.mode).isEqualTo(TeamBuildingMode.AUCTION)
        assertThat(room.budget).isEqualTo(300)
        assertThat(room.teamCount).isEqualTo(2)
        assertThat(room.teamSize).isEqualTo(2)
    }

    @Test
    fun `드래프트 템플릿으로 방을 생성하면 순서 전략이 설정된다`() {
        templateFetcher.addTemplate(
            2L,
            TemplateSnapshot(mode = TeamBuildingMode.DRAFT, teamCount = 2, teamSize = 2, budget = null, draftOrderStrategy = DraftOrderStrategy.SNAKE),
            listOf(TemplatePlayerSnapshot("선수1", 0), TemplatePlayerSnapshot("선수2", 1)),
        )

        val room = cut.create(2L, "호스트")

        assertThat(room.mode).isEqualTo(TeamBuildingMode.DRAFT)
        assertThat(room.draftOrderStrategy).isEqualTo(DraftOrderStrategy.SNAKE)
        assertThat(room.budget).isNull()
    }

    @RepeatedTest(10)
    fun `방 생성 시 6자리 영대문자+숫자 코드가 발급된다`() {
        templateFetcher.addTemplate(
            1L,
            TemplateSnapshot(mode = TeamBuildingMode.AUCTION, teamCount = 2, teamSize = 2, budget = 300, draftOrderStrategy = null),
            emptyList(),
        )

        val room = cut.create(1L, "호스트")

        assertThat(room.code).hasSize(6)
        assertThat(room.code).matches("[A-Z0-9]{6}")
    }

    @Test
    fun `방 생성 시 호스트가 첫 번째 팀장으로 등록된다`() {
        templateFetcher.addTemplate(
            1L,
            TemplateSnapshot(mode = TeamBuildingMode.AUCTION, teamCount = 2, teamSize = 2, budget = 300, draftOrderStrategy = null),
            emptyList(),
        )

        val room = cut.create(1L, "호스트닉네임")

        val leaders = leaderRepo.findByRoomId(room.roomId)
        assertThat(leaders).hasSize(1)
        assertThat(leaders.first().nickname).isEqualTo("호스트닉네임")
        assertThat(leaders.first().teamLeaderId).isEqualTo(room.hostId)
        assertThat(leaders.first().remainingBudget).isEqualTo(300)
    }

    @Test
    fun `방 생성 시 템플릿 선수 목록이 방 선수로 복사된다`() {
        templateFetcher.addTemplate(
            1L,
            TemplateSnapshot(mode = TeamBuildingMode.AUCTION, teamCount = 2, teamSize = 2, budget = 300, draftOrderStrategy = null),
            listOf(TemplatePlayerSnapshot("선수A", 0), TemplatePlayerSnapshot("선수B", 1), TemplatePlayerSnapshot("선수C", 2)),
        )

        val room = cut.create(1L, "호스트")
        val roomPlayers = playerRepo.findByRoomId(room.roomId)

        assertThat(roomPlayers).hasSize(3)
        assertThat(roomPlayers.map { it.name }).containsExactly("선수A", "선수B", "선수C")
        assertThat(roomPlayers).allMatch { it.status == PlayerStatus.AVAILABLE }
    }
}
