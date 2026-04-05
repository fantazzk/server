@file:Suppress("ktlint:standard:no-wildcard-imports")

package com.naminhyeok.fantazzk.room

import com.naminhyeok.fantazzk.room.application.CreateRoom
import com.naminhyeok.fantazzk.room.application.GetRoom
import com.naminhyeok.fantazzk.room.application.JoinRoom
import com.naminhyeok.fantazzk.room.application.PickDraft
import com.naminhyeok.fantazzk.room.application.PlaceBid
import com.naminhyeok.fantazzk.room.application.SettleAuction
import com.naminhyeok.fantazzk.room.application.StartRoom
import com.naminhyeok.fantazzk.room.domain.*
import com.naminhyeok.fantazzk.template.TemplateBlueprint
import com.naminhyeok.fantazzk.template.TemplateCatalog
import com.naminhyeok.fantazzk.template.TemplateDraftOrderStrategy
import com.naminhyeok.fantazzk.template.TemplateId
import com.naminhyeok.fantazzk.template.TemplateMode
import com.naminhyeok.fantazzk.template.TemplatePlayerBlueprint
import com.ninjasquad.springmockk.MockkBean
import io.mockk.every
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.modulith.test.ApplicationModuleTest

@ApplicationModuleTest(
    module = "room",
    verifyAutomatically = false,
)
class RoomModuleIntegrationTest {
    @MockkBean(relaxed = true)
    lateinit var templateCatalog: TemplateCatalog

    @Autowired
    lateinit var roomCreateService: CreateRoom

    @Autowired
    lateinit var roomStartService: StartRoom

    @Autowired
    lateinit var roomFinder: GetRoom

    @Autowired
    lateinit var roomJoinService: JoinRoom

    @Autowired
    lateinit var placeBid: PlaceBid

    @Autowired
    lateinit var settleAuction: SettleAuction

    @Autowired
    lateinit var draftService: PickDraft

    @Autowired
    lateinit var templateCatalogBean: TemplateCatalog

    @Test
    fun `방 모듈은 템플릿 계약 빈과 함께 부팅된다`() {
        assertThat(templateCatalogBean).isNotNull()
        assertThat(roomCreateService).isNotNull()
        assertThat(roomStartService).isNotNull()
    }

    @Test
    fun `방 생성은 대기 상태와 호스트 팀장을 갖는 방을 만든다`() {
        every { templateCatalog.getTemplateBlueprint(TemplateId(1L)) } returns
            TemplateBlueprint(
                templateId = TemplateId(1L),
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

        val room = roomCreateService.create(TemplateId(1L), "호스트")

        assertThat(room.status).isEqualTo(RoomStatus.WAITING)
        assertThat(room.leaders.map { it.nickname }).containsExactly("호스트")
    }

    @Test
    fun `방 생성 후 애그리거트 조회 서비스로 즉시 조회할 수 있다`() {
        every { templateCatalog.getTemplateBlueprint(TemplateId(1L)) } returns
            TemplateBlueprint(
                templateId = TemplateId(1L),
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

        val createdRoom = roomCreateService.create(TemplateId(1L), "호스트")

        val foundRoom = roomFinder.get(createdRoom.code)

        assertThat(foundRoom.code).isEqualTo(createdRoom.code)
        assertThat(foundRoom.status).isEqualTo(RoomStatus.WAITING)
        assertThat(foundRoom.leaders.map { it.nickname }).containsExactly("호스트")
    }

    @Test
    fun `방 참가 서비스는 JPA 저장소에 팀장 추가를 반영한다`() {
        every { templateCatalog.getTemplateBlueprint(TemplateId(1L)) } returns
            TemplateBlueprint(
                templateId = TemplateId(1L),
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

        val createdRoom = roomCreateService.create(TemplateId(1L), "호스트")

        roomJoinService.join(createdRoom.code, "게스트")

        val foundRoom = roomFinder.get(createdRoom.code)
        assertThat(foundRoom.leaders.map { it.nickname }).containsExactly("호스트", "게스트")
    }

    @Test
    fun `경매 입찰과 정산 서비스는 JPA 연관관계 변경을 영속화한다`() {
        every { templateCatalog.getTemplateBlueprint(TemplateId(1L)) } returns
            TemplateBlueprint(
                templateId = TemplateId(1L),
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

        val createdRoom = roomCreateService.create(TemplateId(1L), "호스트")
        val guestLeader = roomJoinService.join(createdRoom.code, "게스트")
        roomStartService.start(createdRoom.code)

        placeBid.place(createdRoom.code, guestLeader.teamLeaderId, 150)
        settleAuction.settle(createdRoom.code)

        val foundRoom = roomFinder.get(createdRoom.code)
        assertThat(foundRoom.members).hasSize(1)
        assertThat(foundRoom.members.single().teamLeaderId).isEqualTo(guestLeader.teamLeaderId)
        assertThat(foundRoom.leaders.single { it.teamLeaderId == guestLeader.teamLeaderId }.remainingBudget).isEqualTo(150)
    }

    @Test
    fun `드래프트 지명 서비스는 JPA 연관관계 변경을 영속화한다`() {
        every { templateCatalog.getTemplateBlueprint(TemplateId(2L)) } returns
            TemplateBlueprint(
                templateId = TemplateId(2L),
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

        val createdRoom = roomCreateService.create(TemplateId(2L), "호스트")
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
