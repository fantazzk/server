package com.naminhyeok.fantazzk.room

import com.naminhyeok.fantazzk.room.application.AuctionService
import com.naminhyeok.fantazzk.room.application.DraftService
import com.naminhyeok.fantazzk.room.application.RoomCreateService
import com.naminhyeok.fantazzk.room.application.RoomFinder
import com.naminhyeok.fantazzk.room.application.RoomJoinService
import com.naminhyeok.fantazzk.room.application.RoomStartService
import com.naminhyeok.fantazzk.template.TemplateBlueprint
import com.naminhyeok.fantazzk.template.TemplateCatalog
import com.naminhyeok.fantazzk.template.TemplateDraftOrderStrategy
import com.naminhyeok.fantazzk.template.TemplateMode
import com.naminhyeok.fantazzk.template.TemplatePlayerBlueprint
import com.ninjasquad.springmockk.MockkBean
import io.mockk.every
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.modulith.test.ApplicationModuleTest
import org.springframework.modulith.test.PublishedEvents

@ApplicationModuleTest(
    module = "room",
    verifyAutomatically = false,
)
class RoomModuleIntegrationTest {
    @MockkBean(relaxed = true)
    lateinit var templateCatalog: TemplateCatalog

    @Autowired
    lateinit var roomCreateService: RoomCreateService

    @Autowired
    lateinit var roomStartService: RoomStartService

    @Autowired
    lateinit var roomFinder: RoomFinder

    @Autowired
    lateinit var roomJoinService: RoomJoinService

    @Autowired
    lateinit var auctionService: AuctionService

    @Autowired
    lateinit var draftService: DraftService

    @Autowired
    lateinit var templateCatalogBean: TemplateCatalog

    @Test
    fun `방 모듈은 템플릿 계약 빈과 함께 부팅된다`() {
        assertThat(templateCatalogBean).isNotNull()
        assertThat(roomCreateService).isNotNull()
        assertThat(roomStartService).isNotNull()
    }

    @Test
    fun `방 생성은 생성 이벤트를 발행한다`(publishedEvents: PublishedEvents) {
        every { templateCatalog.getTemplateBlueprint(1L) } returns
            TemplateBlueprint(
                templateId = 1L,
                mode = TemplateMode.AUCTION,
                teamCount = 2,
                teamSize = 2,
                budget = 300,
                draftOrderStrategy = null,
                players =
                    listOf(
                        TemplatePlayerBlueprint(name = "선수1", displayOrder = 0),
                        TemplatePlayerBlueprint(name = "선수2", displayOrder = 1),
                    ),
            )

        val room = roomCreateService.create(1L, "호스트")

        val events = publishedEvents.ofType(RoomCreated::class.java).matching { it.code == room.code }.toList()
        assertThat(events).hasSize(1)
    }

    @Test
    fun `방 생성 후 애그리거트 조회 서비스로 즉시 조회할 수 있다`() {
        every { templateCatalog.getTemplateBlueprint(1L) } returns
            TemplateBlueprint(
                templateId = 1L,
                mode = TemplateMode.AUCTION,
                teamCount = 2,
                teamSize = 2,
                budget = 300,
                draftOrderStrategy = null,
                players =
                    listOf(
                        TemplatePlayerBlueprint(name = "선수1", displayOrder = 0),
                        TemplatePlayerBlueprint(name = "선수2", displayOrder = 1),
                    ),
            )

        val createdRoom = roomCreateService.create(1L, "호스트")

        val foundRoom = roomFinder.get(createdRoom.code)

        assertThat(foundRoom.code).isEqualTo(createdRoom.code)
        assertThat(foundRoom.status).isEqualTo(RoomStatus.WAITING)
        assertThat(foundRoom.leaders.map { it.nickname }).containsExactly("호스트")
    }

    @Test
    fun `방 참가 서비스는 JPA 저장소에 팀장 추가를 반영한다`() {
        every { templateCatalog.getTemplateBlueprint(1L) } returns
            TemplateBlueprint(
                templateId = 1L,
                mode = TemplateMode.AUCTION,
                teamCount = 2,
                teamSize = 2,
                budget = 300,
                draftOrderStrategy = null,
                players =
                    listOf(
                        TemplatePlayerBlueprint(name = "선수1", displayOrder = 0),
                        TemplatePlayerBlueprint(name = "선수2", displayOrder = 1),
                    ),
            )

        val createdRoom = roomCreateService.create(1L, "호스트")

        roomJoinService.join(createdRoom.code, "게스트")

        val foundRoom = roomFinder.get(createdRoom.code)
        assertThat(foundRoom.leaders.map { it.nickname }).containsExactly("호스트", "게스트")
    }

    @Test
    fun `경매 입찰과 정산 서비스는 JPA 연관관계 변경을 영속화한다`() {
        every { templateCatalog.getTemplateBlueprint(1L) } returns
            TemplateBlueprint(
                templateId = 1L,
                mode = TemplateMode.AUCTION,
                teamCount = 2,
                teamSize = 2,
                budget = 300,
                draftOrderStrategy = null,
                players =
                    listOf(
                        TemplatePlayerBlueprint(name = "선수1", displayOrder = 0),
                        TemplatePlayerBlueprint(name = "선수2", displayOrder = 1),
                    ),
            )

        val createdRoom = roomCreateService.create(1L, "호스트")
        val guestLeader = roomJoinService.join(createdRoom.code, "게스트")
        roomStartService.start(createdRoom.code)

        auctionService.placeBid(createdRoom.code, guestLeader.teamLeaderId, 150)
        auctionService.settle(createdRoom.code)

        val foundRoom = roomFinder.get(createdRoom.code)
        assertThat(foundRoom.members).hasSize(1)
        assertThat(foundRoom.members.single().teamLeaderId).isEqualTo(guestLeader.teamLeaderId)
        assertThat(foundRoom.leaders.single { it.teamLeaderId == guestLeader.teamLeaderId }.remainingBudget).isEqualTo(150)
    }

    @Test
    fun `드래프트 지명 서비스는 JPA 연관관계 변경을 영속화한다`() {
        every { templateCatalog.getTemplateBlueprint(2L) } returns
            TemplateBlueprint(
                templateId = 2L,
                mode = TemplateMode.DRAFT,
                teamCount = 2,
                teamSize = 2,
                budget = null,
                draftOrderStrategy = TemplateDraftOrderStrategy.SNAKE,
                players =
                    listOf(
                        TemplatePlayerBlueprint(name = "선수1", displayOrder = 0),
                        TemplatePlayerBlueprint(name = "선수2", displayOrder = 1),
                    ),
            )

        val createdRoom = roomCreateService.create(2L, "호스트")
        val hostLeaderId = roomFinder.get(createdRoom.code).leaders.single().teamLeaderId
        roomJoinService.join(createdRoom.code, "게스트")
        roomStartService.start(createdRoom.code)

        draftService.pick(createdRoom.code, hostLeaderId, "선수1")

        val foundRoom = roomFinder.get(createdRoom.code)
        assertThat(foundRoom.members).hasSize(1)
        assertThat(foundRoom.members.single().teamLeaderId).isEqualTo(hostLeaderId)
        assertThat(foundRoom.players.single { it.name == "선수1" }.status).isEqualTo(PlayerStatus.ASSIGNED)
        assertThat(foundRoom.currentTurnIndex).isEqualTo(1)
    }
}
