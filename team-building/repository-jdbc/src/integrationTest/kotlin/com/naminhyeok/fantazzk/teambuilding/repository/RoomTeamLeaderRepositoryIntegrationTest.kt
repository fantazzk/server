package com.naminhyeok.fantazzk.teambuilding.repository

import com.naminhyeok.fantazzk.teambuilding.TeamBuildingMode
import com.naminhyeok.fantazzk.teambuilding.config.TeamBuildingJdbcConfiguration
import com.naminhyeok.fantazzk.teambuilding.room.Room
import com.naminhyeok.fantazzk.teambuilding.room.RoomModel
import com.naminhyeok.fantazzk.teambuilding.room.RoomStatus
import com.naminhyeok.fantazzk.teambuilding.room.RoomTeamLeader
import com.naminhyeok.fantazzk.teambuilding.room.repository.RoomRepository
import com.naminhyeok.fantazzk.teambuilding.room.repository.RoomRepositoryAutoConfiguration
import com.naminhyeok.fantazzk.teambuilding.room.repository.RoomTeamLeaderRepository
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
class RoomTeamLeaderRepositoryIntegrationTest(
    private val roomRepository: RoomRepository,
    private val roomTeamLeaderRepository: RoomTeamLeaderRepository,
) {
    private lateinit var room: RoomModel

    @BeforeEach
    fun setUp() {
        room =
            roomRepository.save(
                Room(code = "TL0001", hostId = "host", status = RoomStatus.WAITING, mode = TeamBuildingMode.AUCTION, teamCount = 2, teamSize = 2, budget = 300),
            )
    }

    @Test
    fun `팀장을 저장하고 방 ID로 조회할 수 있다`() {
        roomTeamLeaderRepository.save(
            RoomTeamLeader(roomId = room.roomId, teamLeaderId = "leader-1", nickname = "팀장1", remainingBudget = 300),
        )
        roomTeamLeaderRepository.save(
            RoomTeamLeader(roomId = room.roomId, teamLeaderId = "leader-2", nickname = "팀장2", remainingBudget = 300),
        )

        val leaders = roomTeamLeaderRepository.findByRoomId(room.roomId)
        assertThat(leaders).hasSize(2)
    }

    @Test
    fun `팀장을 방 ID와 팀장 ID로 조회할 수 있다`() {
        roomTeamLeaderRepository.save(
            RoomTeamLeader(roomId = room.roomId, teamLeaderId = "leader-1", nickname = "팀장1", remainingBudget = 300),
        )

        val found = roomTeamLeaderRepository.findByRoomIdAndTeamLeaderId(room.roomId, "leader-1")
        assertThat(found).isNotNull
        assertThat(found!!.nickname).isEqualTo("팀장1")
        assertThat(found.remainingBudget).isEqualTo(300)
    }

    @Test
    fun `존재하지 않는 팀장 ID로 조회하면 null을 반환한다`() {
        val found = roomTeamLeaderRepository.findByRoomIdAndTeamLeaderId(room.roomId, "nonexistent")
        assertThat(found).isNull()
    }

    @Test
    fun `팀장 예산을 업데이트할 수 있다`() {
        val saved =
            roomTeamLeaderRepository.save(
                RoomTeamLeader(roomId = room.roomId, teamLeaderId = "leader-1", nickname = "팀장1", remainingBudget = 300),
            )

        roomTeamLeaderRepository.save(RoomTeamLeader.from(saved).copy(remainingBudget = 150))

        val found = roomTeamLeaderRepository.findByRoomIdAndTeamLeaderId(room.roomId, "leader-1")
        assertThat(found!!.remainingBudget).isEqualTo(150)
    }
}
