package com.naminhyeok.fantazzk.room.repository

import com.naminhyeok.fantazzk.RootCombinedJdbcConfiguration
import com.naminhyeok.fantazzk.room.PlayerStatus
import com.naminhyeok.fantazzk.room.Room
import com.naminhyeok.fantazzk.room.RoomPlayer
import com.naminhyeok.fantazzk.room.RoomStatus
import com.naminhyeok.fantazzk.room.TeamBuildingMode
import com.naminhyeok.fantazzk.room.config.RoomJdbcConfiguration
import com.naminhyeok.fantazzk.template.config.TemplateJdbcConfiguration
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
    RootCombinedJdbcConfiguration::class,
    RoomJdbcConfiguration::class,
    TemplateJdbcConfiguration::class,
    RoomRepositoryConfiguration::class,
)
@DataJdbcTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@TestConstructor(autowireMode = TestConstructor.AutowireMode.ALL)
class RoomPlayerRepositoryIntegrationTest(
    private val roomRepository: RoomRepository,
    private val cut: RoomPlayerRepository,
) {
    private lateinit var room: Room

    @BeforeEach
    fun setUp() {
        room =
            roomRepository.save(
                Room(
                    code = "PL0001",
                    hostId = "host",
                    status = RoomStatus.IN_PROGRESS,
                    mode = TeamBuildingMode.AUCTION,
                    teamCount = 2,
                    teamSize = 2,
                    budget = 300,
                ),
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
    fun `방에 선수가 없으면 빈 목록을 반환한다`() {
        assertThat(cut.findByRoomId(room.roomId)).isEmpty()
    }

    @Test
    fun `방 선수 목록은 displayOrder 기준으로 정렬되고 다른 방 선수는 제외된다`() {
        val anotherRoom =
            roomRepository.save(
                Room(
                    code = "PL0002",
                    hostId = "host-2",
                    status = RoomStatus.IN_PROGRESS,
                    mode = TeamBuildingMode.AUCTION,
                    teamCount = 2,
                    teamSize = 2,
                    budget = 300,
                ),
            )

        cut.saveAll(
            listOf(
                RoomPlayer(roomId = room.roomId, name = "세번째", displayOrder = 2),
                RoomPlayer(roomId = room.roomId, name = "첫번째", displayOrder = 0),
                RoomPlayer(roomId = room.roomId, name = "두번째", displayOrder = 1),
                RoomPlayer(roomId = anotherRoom.roomId, name = "다른방선수", displayOrder = 0),
            ),
        )

        val players = cut.findByRoomId(room.roomId)
        assertThat(players.map { it.name }).containsExactly("첫번째", "두번째", "세번째")
    }

    @Test
    fun `가용 선수 조회는 현재 방에서 displayOrder가 가장 낮은 AVAILABLE 선수만 반환한다`() {
        val anotherRoom =
            roomRepository.save(
                Room(
                    code = "PL0003",
                    hostId = "host-3",
                    status = RoomStatus.IN_PROGRESS,
                    mode = TeamBuildingMode.AUCTION,
                    teamCount = 2,
                    teamSize = 2,
                    budget = 300,
                ),
            )

        cut.saveAll(
            listOf(
                RoomPlayer(roomId = room.roomId, name = "선수1", status = PlayerStatus.ASSIGNED, displayOrder = 0),
                RoomPlayer(roomId = room.roomId, name = "선수2", displayOrder = 1),
                RoomPlayer(roomId = room.roomId, name = "선수3", displayOrder = 2),
                RoomPlayer(roomId = anotherRoom.roomId, name = "다른방선수", displayOrder = 0),
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

        cut.save(saved.copy(status = PlayerStatus.ASSIGNED))

        val found = cut.findByRoomId(room.roomId)
        assertThat(found.first().status).isEqualTo(PlayerStatus.ASSIGNED)
    }
}
