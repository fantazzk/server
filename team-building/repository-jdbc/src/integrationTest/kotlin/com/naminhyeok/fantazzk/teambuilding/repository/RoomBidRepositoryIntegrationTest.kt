package com.naminhyeok.fantazzk.teambuilding.repository

import com.naminhyeok.fantazzk.teambuilding.TeamBuildingMode
import com.naminhyeok.fantazzk.teambuilding.config.TeamBuildingJdbcConfiguration
import com.naminhyeok.fantazzk.teambuilding.room.Room
import com.naminhyeok.fantazzk.teambuilding.room.RoomBid
import com.naminhyeok.fantazzk.teambuilding.room.RoomModel
import com.naminhyeok.fantazzk.teambuilding.room.RoomStatus
import com.naminhyeok.fantazzk.teambuilding.room.repository.RoomBidRepository
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
class RoomBidRepositoryIntegrationTest(
    private val roomRepository: RoomRepository,
    private val roomBidRepository: RoomBidRepository,
) {
    private lateinit var room: RoomModel

    @BeforeEach
    fun setUp() {
        room =
            roomRepository.save(
                Room(code = "BD0001", hostId = "host", status = RoomStatus.IN_PROGRESS, mode = TeamBuildingMode.AUCTION, teamCount = 2, teamSize = 2, budget = 300, currentAuctionRound = 1),
            )
    }

    @Test
    fun `입찰을 저장하고 라운드별로 조회할 수 있다`() {
        roomBidRepository.save(RoomBid(roomId = room.roomId, round = 1, teamLeaderId = "leader-1", amount = 100))
        roomBidRepository.save(RoomBid(roomId = room.roomId, round = 1, teamLeaderId = "leader-2", amount = 150))
        roomBidRepository.save(RoomBid(roomId = room.roomId, round = 2, teamLeaderId = "leader-1", amount = 200))

        val round1Bids = roomBidRepository.findByRoomIdAndRound(room.roomId, 1)
        assertThat(round1Bids).hasSize(2)

        val round2Bids = roomBidRepository.findByRoomIdAndRound(room.roomId, 2)
        assertThat(round2Bids).hasSize(1)
    }

    @Test
    fun `라운드별 최고 입찰을 조회할 수 있다`() {
        roomBidRepository.save(RoomBid(roomId = room.roomId, round = 1, teamLeaderId = "leader-1", amount = 100))
        roomBidRepository.save(RoomBid(roomId = room.roomId, round = 1, teamLeaderId = "leader-2", amount = 150))

        val highest = roomBidRepository.findHighestByRoomIdAndRound(room.roomId, 1)
        assertThat(highest).isNotNull
        assertThat(highest!!.amount).isEqualTo(150)
        assertThat(highest.teamLeaderId).isEqualTo("leader-2")
    }

    @Test
    fun `입찰이 없는 라운드의 최고 입찰은 null이다`() {
        val highest = roomBidRepository.findHighestByRoomIdAndRound(room.roomId, 99)
        assertThat(highest).isNull()
    }
}
