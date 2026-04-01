package com.naminhyeok.fantazzk.room.repository

import com.naminhyeok.fantazzk.RootCombinedJdbcConfiguration
import com.naminhyeok.fantazzk.room.Room
import com.naminhyeok.fantazzk.room.RoomModel
import com.naminhyeok.fantazzk.room.RoomStatus
import com.naminhyeok.fantazzk.room.RoomTeamMember
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
    fun `방 ID로 조회하면 assignOrder 순으로 반환한다`() {
        cut.save(
            RoomTeamMember(roomId = room.roomId, teamLeaderId = "leader-1", playerName = "세번째", assignOrder = 2),
        )
        cut.save(
            RoomTeamMember(roomId = room.roomId, teamLeaderId = "leader-1", playerName = "첫번째", assignOrder = 0),
        )
        cut.save(
            RoomTeamMember(roomId = room.roomId, teamLeaderId = "leader-1", playerName = "두번째", assignOrder = 1),
        )

        val members = cut.findByRoomId(room.roomId)
        assertThat(members.map { it.playerName }).containsExactly("첫번째", "두번째", "세번째")
    }

    @Test
    fun `방에 팀 멤버가 없으면 빈 목록을 반환한다`() {
        assertThat(cut.findByRoomId(room.roomId)).isEmpty()
    }

    @Test
    fun `방 ID로 조회하면 다른 방 멤버는 제외된다`() {
        val anotherRoom =
            roomRepository.save(
                Room(
                    code = "TM0002",
                    hostId = "host-2",
                    status = RoomStatus.IN_PROGRESS,
                    mode = TeamBuildingMode.AUCTION,
                    teamCount = 2,
                    teamSize = 2,
                    budget = 300,
                ),
            )

        cut.save(
            RoomTeamMember(roomId = room.roomId, teamLeaderId = "leader-1", playerName = "현재방선수", assignOrder = 0),
        )
        cut.save(
            RoomTeamMember(roomId = anotherRoom.roomId, teamLeaderId = "leader-1", playerName = "다른방선수", assignOrder = 0),
        )

        val members = cut.findByRoomId(room.roomId)
        assertThat(members.map { it.playerName }).containsExactly("현재방선수")
    }

    @Test
    fun `팀장 ID로 조회하면 assignOrder 순으로 반환한다`() {
        cut.save(
            RoomTeamMember(roomId = room.roomId, teamLeaderId = "leader-1", playerName = "세번째", assignOrder = 2),
        )
        cut.save(
            RoomTeamMember(roomId = room.roomId, teamLeaderId = "leader-1", playerName = "첫번째", assignOrder = 0),
        )
        cut.save(
            RoomTeamMember(roomId = room.roomId, teamLeaderId = "leader-1", playerName = "두번째", assignOrder = 1),
        )

        val leaderMembers = cut.findByRoomIdAndTeamLeaderId(room.roomId, "leader-1")
        assertThat(leaderMembers.map { it.playerName }).containsExactly("첫번째", "두번째", "세번째")
    }

    @Test
    fun `팀장 ID로 조회하면 같은 방의 해당 팀장 멤버만 반환한다`() {
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
    fun `같은 팀장 ID라도 다른 방 멤버는 조회되지 않는다`() {
        val anotherRoom =
            roomRepository.save(
                Room(
                    code = "TM0004",
                    hostId = "host-4",
                    status = RoomStatus.IN_PROGRESS,
                    mode = TeamBuildingMode.AUCTION,
                    teamCount = 2,
                    teamSize = 2,
                    budget = 300,
                ),
            )

        cut.save(
            RoomTeamMember(roomId = room.roomId, teamLeaderId = "leader-1", playerName = "현재방선수", assignOrder = 0),
        )
        cut.save(
            RoomTeamMember(roomId = anotherRoom.roomId, teamLeaderId = "leader-1", playerName = "다른방선수", assignOrder = 0),
        )

        val leaderMembers = cut.findByRoomIdAndTeamLeaderId(room.roomId, "leader-1")
        assertThat(leaderMembers.map { it.playerName }).containsExactly("현재방선수")
    }

    @Test
    fun `팀장 ID에 해당하는 멤버가 없으면 빈 목록을 반환한다`() {
        assertThat(cut.findByRoomIdAndTeamLeaderId(room.roomId, "leader-1")).isEmpty()
    }

    @Test
    fun `기존 팀 멤버를 저장하면 수정 내용이 반영된다`() {
        val saved =
            cut.save(
                RoomTeamMember(roomId = room.roomId, teamLeaderId = "leader-1", playerName = "선수1", assignOrder = 0),
            )

        cut.save(
            RoomTeamMember(
                roomTeamMemberId = saved.roomTeamMemberId,
                roomId = saved.roomId,
                teamLeaderId = saved.teamLeaderId,
                playerName = "수정선수",
                assignOrder = 3,
                createdAt = saved.createdAt,
                updatedAt = saved.updatedAt,
            ),
        )

        val leaderMembers = cut.findByRoomIdAndTeamLeaderId(room.roomId, "leader-1")
        assertThat(leaderMembers).hasSize(1)
        assertThat(leaderMembers.single().playerName).isEqualTo("수정선수")
        assertThat(leaderMembers.single().assignOrder).isEqualTo(3)
    }

    @Test
    fun `방의 팀 멤버 수를 셀 수 있다`() {
        cut.save(
            RoomTeamMember(roomId = room.roomId, teamLeaderId = "leader-1", playerName = "선수1", assignOrder = 0),
        )

        assertThat(cut.countByRoomId(room.roomId)).isEqualTo(1)
    }

    @Test
    fun `방과 팀장 기준 카운트는 해당 범위의 멤버만 센다`() {
        val anotherRoom =
            roomRepository.save(
                Room(
                    code = "TM0003",
                    hostId = "host-3",
                    status = RoomStatus.IN_PROGRESS,
                    mode = TeamBuildingMode.AUCTION,
                    teamCount = 2,
                    teamSize = 2,
                    budget = 300,
                ),
            )

        cut.save(
            RoomTeamMember(roomId = room.roomId, teamLeaderId = "leader-1", playerName = "선수1", assignOrder = 0),
        )
        cut.save(
            RoomTeamMember(roomId = room.roomId, teamLeaderId = "leader-1", playerName = "선수2", assignOrder = 1),
        )
        cut.save(
            RoomTeamMember(roomId = room.roomId, teamLeaderId = "leader-2", playerName = "선수3", assignOrder = 0),
        )
        cut.save(
            RoomTeamMember(roomId = anotherRoom.roomId, teamLeaderId = "leader-1", playerName = "다른방선수", assignOrder = 0),
        )

        assertThat(cut.countByRoomId(room.roomId)).isEqualTo(3)
        assertThat(cut.countByRoomIdAndTeamLeaderId(room.roomId, "leader-1")).isEqualTo(2)
        assertThat(cut.countByRoomIdAndTeamLeaderId(room.roomId, "leader-2")).isEqualTo(1)
    }

    @Test
    fun `팀 멤버가 없으면 0을 반환한다`() {
        assertThat(cut.countByRoomId(room.roomId)).isEqualTo(0)
        assertThat(cut.countByRoomIdAndTeamLeaderId(room.roomId, "leader-1")).isEqualTo(0)
    }
}
