package com.naminhyeok.fantazzk.teambuilding.repository

import com.naminhyeok.fantazzk.teambuilding.TeamBuildingMode
import com.naminhyeok.fantazzk.teambuilding.config.TeamBuildingJdbcConfiguration
import com.naminhyeok.fantazzk.teambuilding.room.PlayerStatus
import com.naminhyeok.fantazzk.teambuilding.room.Room
import com.naminhyeok.fantazzk.teambuilding.room.RoomModel
import com.naminhyeok.fantazzk.teambuilding.room.RoomPlayer
import com.naminhyeok.fantazzk.teambuilding.room.RoomStatus
import com.naminhyeok.fantazzk.teambuilding.room.repository.RoomPlayerRepository
import com.naminhyeok.fantazzk.teambuilding.room.repository.RoomRepository
import com.naminhyeok.fantazzk.teambuilding.room.repository.RoomRepositoryAutoConfiguration
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
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
class RoomPlayerRepositoryIntegrationTest(
    private val roomRepository: RoomRepository,
    private val cut: RoomPlayerRepository,
) {
    private lateinit var room: RoomModel

    @BeforeEach
    fun setUp() {
        room =
            roomRepository.save(
                Room(code = "PL0001", hostId = "host", status = RoomStatus.IN_PROGRESS, mode = TeamBuildingMode.AUCTION, teamCount = 2, teamSize = 2, budget = 300),
            )
    }

    @Test
    fun `선수를 저장하고 방 ID로 조회할 수 있다`() {
        cut.saveAll(
            listOf(
                RoomPlayer(roomId = room.roomId, name = "선수1", displayOrder = 0),
                RoomPlayer(roomId = room.roomId, name = "선수2", displayOrder = 1),
            ),
        )

        val players = cut.findByRoomId(room.roomId)
        assertThat(players).hasSize(2)
        assertThat(players.map { it.name }).containsExactly("선수1", "선수2")
    }

    @Test
    fun `가용 선수 중 displayOrder가 가장 낮은 선수를 조회한다`() {
        cut.saveAll(
            listOf(
                RoomPlayer(roomId = room.roomId, name = "선수1", status = PlayerStatus.ASSIGNED, displayOrder = 0),
                RoomPlayer(roomId = room.roomId, name = "선수2", displayOrder = 1),
                RoomPlayer(roomId = room.roomId, name = "선수3", displayOrder = 2),
            ),
        )

        val firstAvailable = cut.findFirstAvailable(room.roomId)
        assertThat(firstAvailable).isNotNull
        assertThat(firstAvailable!!.name).isEqualTo("선수2")
    }

    @Test
    fun `모든 선수가 배정되면 가용 선수가 없다`() {
        cut.saveAll(
            listOf(
                RoomPlayer(roomId = room.roomId, name = "선수1", status = PlayerStatus.ASSIGNED, displayOrder = 0),
            ),
        )

        assertThat(cut.findFirstAvailable(room.roomId)).isNull()
    }

    @Test
    fun `선수 상태를 업데이트할 수 있다`() {
        val saved =
            cut.saveAll(
                listOf(RoomPlayer(roomId = room.roomId, name = "선수1", displayOrder = 0)),
            ).first()

        cut.save(RoomPlayer.from(saved).copy(status = PlayerStatus.ASSIGNED))

        val found = cut.findByRoomId(room.roomId)
        assertThat(found.first().status).isEqualTo(PlayerStatus.ASSIGNED)
    }
}
