package com.naminhyeok.fantazzk.room

import com.naminhyeok.fantazzk.room.application.RoomCreateService
import com.naminhyeok.fantazzk.room.application.RoomStartService
import com.naminhyeok.fantazzk.room.query.RoomViewProjectionRepository
import com.naminhyeok.fantazzk.template.spi.TemplateLookup
import com.naminhyeok.fantazzk.template.spi.TemplateMode
import com.naminhyeok.fantazzk.template.spi.TemplatePlayerSnapshot
import com.naminhyeok.fantazzk.template.spi.TemplateSnapshot
import com.ninjasquad.springmockk.MockkBean
import io.mockk.every
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.modulith.test.ApplicationModuleTest
import org.springframework.modulith.test.PublishedEvents
import org.springframework.modulith.test.Scenario

@ApplicationModuleTest(
    module = "room",
    verifyAutomatically = false,
)
class RoomModuleIntegrationTest {
    @MockkBean(relaxed = true)
    lateinit var templateLookup: TemplateLookup

    @Autowired
    lateinit var roomCreateService: RoomCreateService

    @Autowired
    lateinit var roomStartService: RoomStartService

    @Autowired
    lateinit var roomViewProjectionRepository: RoomViewProjectionRepository

    @Test
    fun `room module boots with direct dependencies`() {
        assertThat(roomCreateService).isNotNull()
        assertThat(roomStartService).isNotNull()
    }

    @Test
    fun `room create publishes RoomCreated`(publishedEvents: PublishedEvents) {
        every { templateLookup.getTemplate(1L) } returns
            TemplateSnapshot(
                mode = TemplateMode.AUCTION,
                teamCount = 2,
                teamSize = 2,
                budget = 300,
                draftOrderStrategy = null,
                players =
                    listOf(
                        TemplatePlayerSnapshot(name = "선수1", displayOrder = 0),
                        TemplatePlayerSnapshot(name = "선수2", displayOrder = 1),
                    ),
            )

        val room = roomCreateService.create(1L, "호스트")

        val events = publishedEvents.ofType(RoomCreated::class.java).matching { it.code == room.code }.toList()
        assertThat(events).hasSize(1)
    }

    @Test
    fun `room create updates room query projection`(scenario: Scenario) {
        every { templateLookup.getTemplate(1L) } returns
            TemplateSnapshot(
                mode = TemplateMode.AUCTION,
                teamCount = 2,
                teamSize = 2,
                budget = 300,
                draftOrderStrategy = null,
                players =
                    listOf(
                        TemplatePlayerSnapshot(name = "선수1", displayOrder = 0),
                        TemplatePlayerSnapshot(name = "선수2", displayOrder = 1),
                    ),
            )

        scenario
            .stimulate { roomCreateService.create(1L, "호스트") }
            .andWaitForStateChange({ roomViewProjectionRepository.findAll().size }) { it > 0 }
            .andVerify { projectionCount ->
                assertThat(projectionCount).isGreaterThan(0)
            }
    }
}
