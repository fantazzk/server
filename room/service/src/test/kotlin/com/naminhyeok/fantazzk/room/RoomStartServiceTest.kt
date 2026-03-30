package com.naminhyeok.fantazzk.room

import com.naminhyeok.fantazzk.room.exception.RoomException
import com.naminhyeok.fantazzk.room.repository.RoomRepository
import com.naminhyeok.fantazzk.room.support.InMemoryRoomRepository
import com.naminhyeok.fantazzk.room.support.InMemoryRoomTeamLeaderRepository
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.EnumSource
import org.junit.jupiter.params.provider.ValueSource

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
                    Room.createAuction(
                        code = "STATE1",
                        hostId = "host",
                        teamCount = 2,
                        teamSize = 2,
                        budget = 300,
                    ).copy(status = status),
                )
            fillLeaders(room)

            assertThatThrownBy { cut.start(room.code) }
                .isInstanceOf(IllegalStateException::class.java)
                .hasMessage("대기 중인 방에서만 시작할 수 있습니다")
        }

        @Test
        fun `존재하지 않는 방은 시작할 수 없다`() {
            assertThatThrownBy { cut.start("NOROOM") }
                .isInstanceOf(RoomException.RoomNotFoundException::class.java)
        }
    }

    @Nested
    inner class `팀장 전제 조건` {
        @ParameterizedTest(name = "팀장이 {0}명이면 시작할 수 없다")
        @ValueSource(ints = [0, 1, 3])
        fun `팀장 수는 teamCount와 정확히 일치해야 한다`(leaderCount: Int) {
            val room = createWaitingRoom(TeamBuildingMode.AUCTION, budget = 300)
            saveLeaders(room, leaderCount)

            assertThatThrownBy { cut.start(room.code) }
                .isInstanceOf(IllegalStateException::class.java)
                .hasMessage("모든 팀장 자리가 채워져야 시작할 수 있습니다")
        }
    }

    @Nested
    inner class `모드별 초기화` {
        @Test
        fun `경매 모드는 currentAuctionRound만 1로 초기화하고 현재 턴은 비운다`() {
            val room =
                createWaitingRoom(
                    mode = TeamBuildingMode.AUCTION,
                    budget = 300,
                    currentTurnIndex = 4,
                    currentAuctionRound = 8,
                )
            saveLeaders(room, room.teamCount)

            cut.start(room.code)

            val started = roomRepo.findByCode(room.code)!!
            assertThat(started.currentAuctionRound).isEqualTo(1)
            assertThat(started.currentTurnIndex).isNull()
            assertThat(Room.from(started).progress).isEqualTo(RoomProgress.Auction(currentRound = 1))
        }

        @Test
        fun `드래프트 모드는 currentTurnIndex만 0으로 초기화하고 경매 라운드는 비운다`() {
            val room =
                createWaitingRoom(
                    mode = TeamBuildingMode.DRAFT,
                    draftOrderStrategy = DraftOrderStrategy.SNAKE,
                    currentTurnIndex = 3,
                    currentAuctionRound = 7,
                )
            saveLeaders(room, room.teamCount)

            cut.start(room.code)

            val started = roomRepo.findByCode(room.code)!!
            assertThat(started.currentTurnIndex).isEqualTo(0)
            assertThat(started.currentAuctionRound).isNull()
            assertThat(Room.from(started).progress).isEqualTo(RoomProgress.Draft(currentTurnIndex = 0))
        }

        @Test
        fun `legacy 경매 방의 stale draft strategy는 무시하고 시작한다`() {
            val legacyRoomRepo = LegacyRoomRepository(legacyAuctionRoomModel())
            cut = RoomStartServiceImpl(legacyRoomRepo, leaderRepo)
            val room = legacyRoomRepo.findByCode("START2")!!
            saveLeaders(room, room.teamCount)

            cut.start(room.code)

            val started = legacyRoomRepo.findByCode(room.code)!!
            assertThat(started.status).isEqualTo(RoomStatus.IN_PROGRESS)
            assertThat(started.currentAuctionRound).isEqualTo(1)
        }
    }

    private fun legacyAuctionRoomModel(): RoomModel =
        object : RoomModel {
            override val roomId = 99L
            override val code = "START2"
            override val hostId = "host"
            override val status = RoomStatus.WAITING
            override val mode = TeamBuildingMode.AUCTION
            override val teamCount = 2
            override val teamSize = 2
            override val budget = 300
            override val draftOrderStrategy = DraftOrderStrategy.SNAKE
            override val currentTurnIndex: Int? = null
            override val currentAuctionRound: Int? = null
            override val createdAt = java.time.Instant.parse("2025-01-01T00:00:00Z")
            override val updatedAt = java.time.Instant.parse("2025-01-01T00:00:00Z")
        }

    private class LegacyRoomRepository(
        private var room: RoomModel,
    ) : RoomRepository {
        override fun save(room: Room): RoomModel {
            this.room = room
            return room
        }

        override fun findByCode(code: String): RoomModel? = room.takeIf { it.code == code }

        override fun findById(roomId: Long): RoomModel? = room.takeIf { it.roomId == roomId }
    }

    private fun createWaitingRoom(
        mode: TeamBuildingMode,
        budget: Int? = null,
        draftOrderStrategy: DraftOrderStrategy? = null,
        teamCount: Int = 2,
        currentTurnIndex: Int? = null,
        currentAuctionRound: Int? = null,
    ): RoomModel =
        roomRepo.save(
            when (mode) {
                TeamBuildingMode.AUCTION ->
                    Room.createAuction(
                        code = "START1",
                        hostId = "host",
                        teamCount = teamCount,
                        teamSize = 2,
                        budget = requireNotNull(budget) { "경매 방에는 예산이 필요합니다" },
                    )

                TeamBuildingMode.DRAFT ->
                    Room.createDraft(
                        code = "START1",
                        hostId = "host",
                        teamCount = teamCount,
                        teamSize = 2,
                        draftOrderStrategy = requireNotNull(draftOrderStrategy) { "드래프트 방에는 순서 전략이 필요합니다" },
                    )
            }.copy(
                currentTurnIndex = currentTurnIndex,
                currentAuctionRound = currentAuctionRound,
            ),
        )

    private fun fillLeaders(room: RoomModel) {
        saveLeaders(room, room.teamCount)
    }

    private fun saveLeaders(
        room: RoomModel,
        count: Int,
    ) {
        repeat(count) { index ->
            leaderRepo.save(
                RoomTeamLeader(
                    roomId = room.roomId,
                    teamLeaderId = "leader-${index + 1}",
                    nickname = "팀장${index + 1}",
                    remainingBudget = room.budget,
                ),
            )
        }
    }
}
