package com.naminhyeok.fantazzk.room.repository

import com.naminhyeok.fantazzk.room.Room
import com.naminhyeok.fantazzk.room.RoomBid
import com.naminhyeok.fantazzk.room.RoomModel
import com.naminhyeok.fantazzk.room.RoomStatus
import com.naminhyeok.fantazzk.room.TeamBuildingMode
import com.naminhyeok.fantazzk.room.config.RoomJdbcConfiguration
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
    RoomJdbcConfiguration::class,
    RoomRepositoryAutoConfiguration::class,
)
@DataJdbcTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@TestConstructor(autowireMode = TestConstructor.AutowireMode.ALL)
class RoomBidRepositoryIntegrationTest(
    private val roomRepository: RoomRepository,
    private val cut: RoomBidRepository,
) {
    private lateinit var room: RoomModel

    @BeforeEach
    fun setUp() {
        room =
            roomRepository.save(
                Room(
                    code = "BD0001",
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
    fun `입찰을 저장하고 라운드별로 조회할 수 있다`() {
        cut.save(RoomBid(roomId = room.roomId, round = 1, teamLeaderId = "leader-1", amount = 100))
        cut.save(RoomBid(roomId = room.roomId, round = 1, teamLeaderId = "leader-2", amount = 150))
        cut.save(RoomBid(roomId = room.roomId, round = 2, teamLeaderId = "leader-1", amount = 200))

        val round1Bids = cut.findByRoomIdAndRound(room.roomId, 1)
        assertThat(round1Bids).hasSize(2)

        val round2Bids = cut.findByRoomIdAndRound(room.roomId, 2)
        assertThat(round2Bids).hasSize(1)
    }

    @Test
    fun `라운드별 최고 입찰을 조회할 수 있다`() {
        cut.save(RoomBid(roomId = room.roomId, round = 1, teamLeaderId = "leader-1", amount = 100))
        cut.save(RoomBid(roomId = room.roomId, round = 1, teamLeaderId = "leader-2", amount = 150))

        val highest = cut.findHighestByRoomIdAndRound(room.roomId, 1)
        assertThat(highest).isNotNull
        assertThat(highest!!.amount).isEqualTo(150)
        assertThat(highest.teamLeaderId).isEqualTo("leader-2")
    }

    @Test
    fun `입찰이 없는 라운드의 최고 입찰은 null이다`() {
        val highest = cut.findHighestByRoomIdAndRound(room.roomId, 99)
        assertThat(highest).isNull()
    }
}
