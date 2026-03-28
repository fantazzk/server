package com.naminhyeok.fantazzk.teambuilding.repository

import com.naminhyeok.fantazzk.teambuilding.DraftOrderStrategy
import com.naminhyeok.fantazzk.teambuilding.TeamBuildingMode
import com.naminhyeok.fantazzk.teambuilding.config.TeamBuildingJdbcConfiguration
import com.naminhyeok.fantazzk.teambuilding.room.Room
import com.naminhyeok.fantazzk.teambuilding.room.RoomStatus
import com.naminhyeok.fantazzk.teambuilding.room.repository.RoomRepository
import com.naminhyeok.fantazzk.teambuilding.room.repository.RoomRepositoryAutoConfiguration
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.boot.autoconfigure.ImportAutoConfiguration
import org.springframework.boot.data.jdbc.test.autoconfigure.DataJdbcTest
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase
import org.springframework.boot.liquibase.autoconfigure.LiquibaseAutoConfiguration
import org.springframework.test.context.TestConstructor

@ImportAutoConfiguration(
    LiquibaseAutoConfiguration::class,
    TeamBuildingJdbcConfiguration::class,
    RoomRepositoryAutoConfiguration::class,
)
@DataJdbcTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@TestConstructor(autowireMode = TestConstructor.AutowireMode.ALL)
class RoomRepositoryIntegrationTest(
    private val cut: RoomRepository,
) {
    @Test
    fun `방을 저장하고 코드로 조회할 수 있다`() {
        val saved =
            cut.save(
                Room(
                    code = "RM0001",
                    hostId = "host",
                    status = RoomStatus.WAITING,
                    mode = TeamBuildingMode.AUCTION,
                    teamCount = 2,
                    teamSize = 2,
                    budget = 300,
                ),
            )

        assertThat(saved.roomId).isGreaterThan(0)

        val found = cut.findByCode("RM0001")
        assertThat(found).isNotNull
        assertThat(found!!.status).isEqualTo(RoomStatus.WAITING)
        assertThat(found.mode).isEqualTo(TeamBuildingMode.AUCTION)
    }

    @Test
    fun `방을 저장하고 ID로 조회할 수 있다`() {
        val saved =
            cut.save(
                Room(
                    code = "RM0002",
                    hostId = "host",
                    status = RoomStatus.WAITING,
                    mode = TeamBuildingMode.DRAFT,
                    teamCount = 3,
                    teamSize = 4,
                    draftOrderStrategy = DraftOrderStrategy.SNAKE,
                    currentTurnIndex = 0,
                ),
            )

        val found = cut.findById(saved.roomId)
        assertThat(found).isNotNull
        assertThat(found!!.draftOrderStrategy).isEqualTo(DraftOrderStrategy.SNAKE)
        assertThat(found.currentTurnIndex).isEqualTo(0)
    }

    @Test
    fun `방 상태를 업데이트할 수 있다`() {
        val saved =
            cut.save(
                Room(
                    code = "RM0003",
                    hostId = "host",
                    status = RoomStatus.WAITING,
                    mode = TeamBuildingMode.AUCTION,
                    teamCount = 2,
                    teamSize = 2,
                    budget = 300,
                ),
            )

        val updated = Room.from(saved).copy(status = RoomStatus.IN_PROGRESS, currentAuctionRound = 1)
        cut.save(updated)

        val found = cut.findByCode("RM0003")
        assertThat(found!!.status).isEqualTo(RoomStatus.IN_PROGRESS)
        assertThat(found.currentAuctionRound).isEqualTo(1)
    }
}
