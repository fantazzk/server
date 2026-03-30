package com.naminhyeok.fantazzk.room.repository

import com.naminhyeok.fantazzk.room.Room
import com.naminhyeok.fantazzk.room.RoomModel
import com.naminhyeok.fantazzk.room.RoomStatus
import com.naminhyeok.fantazzk.room.RoomTeamLeader
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
class RoomTeamLeaderRepositoryIntegrationTest(
    private val roomRepository: RoomRepository,
    private val cut: RoomTeamLeaderRepository,
) {
    private lateinit var room: RoomModel

    @BeforeEach
    fun setUp() {
        room =
            roomRepository.save(
                Room(
                    code = "TL0001",
                    hostId = "host",
                    status = RoomStatus.WAITING,
                    mode = TeamBuildingMode.AUCTION,
                    teamCount = 2,
                    teamSize = 2,
                    budget = 300,
                ),
            )
    }

    @Test
    fun `팀장을 저장하고 방 ID로 조회할 수 있다`() {
        cut.save(
            RoomTeamLeader(roomId = room.roomId, teamLeaderId = "leader-1", nickname = "팀장1", remainingBudget = 300),
        )
        cut.save(
            RoomTeamLeader(roomId = room.roomId, teamLeaderId = "leader-2", nickname = "팀장2", remainingBudget = 300),
        )

        val leaders = cut.findByRoomId(room.roomId)
        assertThat(leaders).hasSize(2)
    }

    @Test
    fun `방에 팀장이 없으면 빈 목록을 반환한다`() {
        assertThat(cut.findByRoomId(room.roomId)).isEmpty()
    }

    @Test
    fun `방 ID로 조회하면 다른 방 팀장은 제외된다`() {
        val anotherRoom =
            roomRepository.save(
                Room(
                    code = "TL0002",
                    hostId = "other-host",
                    status = RoomStatus.WAITING,
                    mode = TeamBuildingMode.AUCTION,
                    teamCount = 2,
                    teamSize = 2,
                    budget = 300,
                ),
            )

        cut.save(
            RoomTeamLeader(roomId = room.roomId, teamLeaderId = "leader-1", nickname = "현재방팀장", remainingBudget = 300),
        )
        cut.save(
            RoomTeamLeader(roomId = anotherRoom.roomId, teamLeaderId = "leader-2", nickname = "다른방팀장", remainingBudget = 300),
        )

        val leaders = cut.findByRoomId(room.roomId)
        assertThat(leaders).hasSize(1)
        assertThat(leaders.single().nickname).isEqualTo("현재방팀장")
    }

    @Test
    fun `팀장을 방 ID와 팀장 ID로 조회할 수 있다`() {
        cut.save(
            RoomTeamLeader(roomId = room.roomId, teamLeaderId = "leader-1", nickname = "팀장1", remainingBudget = 300),
        )

        val found = cut.findByRoomIdAndTeamLeaderId(room.roomId, "leader-1")
        assertThat(found).isNotNull
        assertThat(found!!.nickname).isEqualTo("팀장1")
        assertThat(found.remainingBudget).isEqualTo(300)
    }

    @Test
    fun `다른 방에만 있는 팀장 ID는 현재 방에서 조회되지 않는다`() {
        val anotherRoom =
            roomRepository.save(
                Room(
                    code = "TL0003",
                    hostId = "other-host",
                    status = RoomStatus.WAITING,
                    mode = TeamBuildingMode.AUCTION,
                    teamCount = 2,
                    teamSize = 2,
                    budget = 300,
                ),
            )

        cut.save(
            RoomTeamLeader(roomId = anotherRoom.roomId, teamLeaderId = "leader-1", nickname = "다른방팀장", remainingBudget = 300),
        )

        assertThat(cut.findByRoomIdAndTeamLeaderId(room.roomId, "leader-1")).isNull()
    }

    @Test
    fun `남은 예산이 null인 팀장도 저장하고 조회할 수 있다`() {
        cut.save(
            RoomTeamLeader(roomId = room.roomId, teamLeaderId = "leader-null", nickname = "드래프트팀장"),
        )

        val found = cut.findByRoomIdAndTeamLeaderId(room.roomId, "leader-null")
        assertThat(found).isNotNull
        assertThat(found!!.nickname).isEqualTo("드래프트팀장")
        assertThat(found.remainingBudget).isNull()
    }

    @Test
    fun `존재하지 않는 팀장 ID로 조회하면 null을 반환한다`() {
        val found = cut.findByRoomIdAndTeamLeaderId(room.roomId, "nonexistent")
        assertThat(found).isNull()
    }

    @Test
    fun `팀장 정보 업데이트 시 닉네임과 예산 변경이 반영된다`() {
        val saved =
            cut.save(
                RoomTeamLeader(roomId = room.roomId, teamLeaderId = "leader-1", nickname = "팀장1"),
            )

        cut.save(RoomTeamLeader.from(saved).copy(nickname = "수정팀장", remainingBudget = 150))

        val found = cut.findByRoomIdAndTeamLeaderId(room.roomId, "leader-1")
        assertThat(found).isNotNull
        assertThat(found!!.nickname).isEqualTo("수정팀장")
        assertThat(found.remainingBudget).isEqualTo(150)
    }
}
