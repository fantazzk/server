package com.naminhyeok.fantazzk.teambuilding.room

import com.naminhyeok.fantazzk.teambuilding.DraftOrderStrategy
import com.naminhyeok.fantazzk.teambuilding.TeamBuildingMode
import com.naminhyeok.fantazzk.teambuilding.exception.RoomNotFoundException
import com.naminhyeok.fantazzk.teambuilding.support.InMemoryRoomRepository
import com.naminhyeok.fantazzk.teambuilding.support.InMemoryRoomTeamLeaderRepository
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.EnumSource

class RoomStartServiceTest {
    private lateinit var roomRepo: InMemoryRoomRepository
    private lateinit var leaderRepo: InMemoryRoomTeamLeaderRepository
    private lateinit var cut: RoomStartService

    @BeforeEach
    fun setUp() {
        roomRepo = InMemoryRoomRepository()
        leaderRepo = InMemoryRoomTeamLeaderRepository()
        cut = RoomStartServiceImpl(roomRepo, leaderRepo)
    }

    @Nested
    inner class `상태 전이 규칙` {
        @Test
        fun `WAITING 상태의 방을 시작하면 IN_PROGRESS로 전환된다`() {
            val room = createWaitingRoom(TeamBuildingMode.AUCTION, budget = 300)
            fillLeaders(room)

            cut.start(room.code)

            val started = roomRepo.findByCode(room.code)!!
            assertThat(started.status).isEqualTo(RoomStatus.IN_PROGRESS)
        }

        @ParameterizedTest(name = "{0} 상태의 방은 시작할 수 없다")
        @EnumSource(value = RoomStatus::class, names = ["WAITING"], mode = EnumSource.Mode.EXCLUDE)
        fun `WAITING이 아닌 상태의 방은 시작할 수 없다`(status: RoomStatus) {
            val room =
                roomRepo.save(
                    Room(
                        code = "STATE1",
                        hostId = "host",
                        status = status,
                        mode = TeamBuildingMode.AUCTION,
                        teamCount = 2,
                        teamSize = 2,
                        budget = 300,
                    ),
                )
            fillLeaders(room)

            assertThatThrownBy { cut.start(room.code) }
                .isInstanceOf(IllegalStateException::class.java)
        }

        @Test
        fun `존재하지 않는 방은 시작할 수 없다`() {
            assertThatThrownBy { cut.start("NOROOM") }
                .isInstanceOf(RoomNotFoundException::class.java)
        }
    }

    @Nested
    inner class `팀장 전제 조건` {
        @Test
        fun `팀장이 부족하면 시작할 수 없다`() {
            val room = createWaitingRoom(TeamBuildingMode.AUCTION, budget = 300)
            leaderRepo.save(RoomTeamLeader(roomId = room.roomId, teamLeaderId = "leader-A", nickname = "팀장A", remainingBudget = 300))

            assertThatThrownBy { cut.start(room.code) }
                .isInstanceOf(IllegalStateException::class.java)
        }

        @Test
        fun `팀장이 한 명도 없으면 시작할 수 없다`() {
            val room = createWaitingRoom(TeamBuildingMode.AUCTION, budget = 300)

            assertThatThrownBy { cut.start(room.code) }
                .isInstanceOf(IllegalStateException::class.java)
        }
    }

    @Nested
    inner class `모드별 초기화` {
        @Test
        fun `경매 모드는 currentAuctionRound를 1로 초기화한다`() {
            val room = createWaitingRoom(TeamBuildingMode.AUCTION, budget = 300)
            fillLeaders(room)

            cut.start(room.code)

            val started = roomRepo.findByCode(room.code)!!
            assertThat(started.currentAuctionRound).isEqualTo(1)
            assertThat(started.currentTurnIndex).isNull()
        }

        @Test
        fun `드래프트 모드는 currentTurnIndex를 0으로 초기화한다`() {
            val room = createWaitingRoom(TeamBuildingMode.DRAFT, draftOrderStrategy = DraftOrderStrategy.SNAKE)
            fillLeaders(room)

            cut.start(room.code)

            val started = roomRepo.findByCode(room.code)!!
            assertThat(started.currentTurnIndex).isEqualTo(0)
            assertThat(started.currentAuctionRound).isNull()
        }
    }

    private fun createWaitingRoom(
        mode: TeamBuildingMode,
        budget: Int? = null,
        draftOrderStrategy: DraftOrderStrategy? = null,
    ): RoomModel =
        roomRepo.save(
            Room(
                code = "START1",
                hostId = "host",
                status = RoomStatus.WAITING,
                mode = mode,
                teamCount = 2,
                teamSize = 2,
                budget = budget,
                draftOrderStrategy = draftOrderStrategy,
            ),
        )

    private fun fillLeaders(room: RoomModel) {
        leaderRepo.save(RoomTeamLeader(roomId = room.roomId, teamLeaderId = "leader-A", nickname = "팀장A", remainingBudget = room.budget))
        leaderRepo.save(RoomTeamLeader(roomId = room.roomId, teamLeaderId = "leader-B", nickname = "팀장B", remainingBudget = room.budget))
    }
}
