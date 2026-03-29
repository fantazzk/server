package com.naminhyeok.fantazzk.room.repository

import com.naminhyeok.fantazzk.room.Room
import com.naminhyeok.fantazzk.room.RoomModel
import com.naminhyeok.fantazzk.room.RoomStatus
import com.naminhyeok.fantazzk.room.RoomTeamMember
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
class RoomTeamMemberRepositoryIntegrationTest(
    private val roomRepository: RoomRepository,
    private val cut: RoomTeamMemberRepository,
) {
    private lateinit var room: RoomModel

    @BeforeEach
    fun setUp() {
        room =
            roomRepository.save(
                Room(
                    code = "TM0001",
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
    fun `팀 멤버를 저장하고 방 ID로 조회할 수 있다`() {
        cut.save(
            RoomTeamMember(roomId = room.roomId, teamLeaderId = "leader-1", playerName = "선수1", assignOrder = 0),
        )
        cut.save(
            RoomTeamMember(roomId = room.roomId, teamLeaderId = "leader-1", playerName = "선수2", assignOrder = 1),
        )

        val members = cut.findByRoomId(room.roomId)
        assertThat(members).hasSize(2)
    }

    @Test
    fun `팀장 ID로 팀 멤버를 조회할 수 있다`() {
        cut.save(
            RoomTeamMember(roomId = room.roomId, teamLeaderId = "leader-1", playerName = "선수1", assignOrder = 0),
        )
        cut.save(
            RoomTeamMember(roomId = room.roomId, teamLeaderId = "leader-2", playerName = "선수2", assignOrder = 1),
        )

        val leaderMembers = cut.findByRoomIdAndTeamLeaderId(room.roomId, "leader-1")
        assertThat(leaderMembers).hasSize(1)
        assertThat(leaderMembers.first().playerName).isEqualTo("선수1")
    }

    @Test
    fun `방의 팀 멤버 수를 셀 수 있다`() {
        cut.save(
            RoomTeamMember(roomId = room.roomId, teamLeaderId = "leader-1", playerName = "선수1", assignOrder = 0),
        )

        assertThat(cut.countByRoomId(room.roomId)).isEqualTo(1)
    }

    @Test
    fun `팀 멤버가 없으면 0을 반환한다`() {
        assertThat(cut.countByRoomId(room.roomId)).isEqualTo(0)
    }
}
