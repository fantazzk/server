package com.naminhyeok.fantazzk.room

import com.naminhyeok.fantazzk.room.application.RoomCreateService
import com.naminhyeok.fantazzk.room.application.RoomFinder
import com.naminhyeok.fantazzk.room.application.RoomStartService
import com.naminhyeok.fantazzk.template.TemplateBlueprint
import com.naminhyeok.fantazzk.template.TemplateCatalog
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
    lateinit var templateCatalogBean: TemplateCatalog

    @Test
    fun `room module boots with template root contract`() {
        assertThat(templateCatalogBean).isNotNull()
        assertThat(roomCreateService).isNotNull()
        assertThat(roomStartService).isNotNull()
    }

    @Test
    fun `room create publishes RoomCreated`(publishedEvents: PublishedEvents) {
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
    fun `room create 후 aggregate finder 로 즉시 조회할 수 있다`() {
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
}
