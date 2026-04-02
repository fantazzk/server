package com.naminhyeok.fantazzk.room

import com.naminhyeok.fantazzk.room.application.RoomJoinService
import com.naminhyeok.fantazzk.room.application.RoomJoinServiceImpl
import com.naminhyeok.fantazzk.room.exception.RoomException
import com.naminhyeok.fantazzk.room.repository.RoomAggregateRepositoryImpl
import com.naminhyeok.fantazzk.room.repository.RoomRepository
import com.naminhyeok.fantazzk.room.support.InMemoryRoomBidRepository
import com.naminhyeok.fantazzk.room.support.InMemoryRoomPlayerRepository
import com.naminhyeok.fantazzk.room.support.InMemoryRoomRepository
import com.naminhyeok.fantazzk.room.support.InMemoryRoomTeamLeaderRepository
import com.naminhyeok.fantazzk.room.support.InMemoryRoomTeamMemberRepository
import io.mockk.mockk
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.EnumSource
import org.junit.jupiter.params.provider.ValueSource
import org.springframework.context.ApplicationEventPublisher

class RoomJoinServiceTest {
    private lateinit var roomRepo: InMemoryRoomRepository
    private lateinit var playerRepo: InMemoryRoomPlayerRepository
    private lateinit var leaderRepo: InMemoryRoomTeamLeaderRepository
    private lateinit var memberRepo: InMemoryRoomTeamMemberRepository
    private lateinit var bidRepo: InMemoryRoomBidRepository
    private lateinit var events: ApplicationEventPublisher
    private lateinit var cut: RoomJoinService

    private lateinit var roomCode: String
    private var roomId: Long = 0L

    @BeforeEach
    fun setUp() {
        roomRepo = InMemoryRoomRepository()
        playerRepo = InMemoryRoomPlayerRepository()
        leaderRepo = InMemoryRoomTeamLeaderRepository()
        memberRepo = InMemoryRoomTeamMemberRepository()
        bidRepo = InMemoryRoomBidRepository()
        events = mockk(relaxed = true)
        cut = RoomJoinServiceImpl(RoomAggregateRepositoryImpl(roomRepo, playerRepo, leaderRepo, memberRepo, bidRepo), events)

        val room =
            roomRepo.save(
                Room.createAuction(
                    code = "JOIN01",
                    hostId = "host",
                    teamCount = 2,
                    teamSize = 2,
                    budget = 300,
                ),
            )
        roomCode = room.code
        roomId = room.roomId

        leaderRepo.save(
            RoomTeamLeader(roomId = roomId, teamLeaderId = "host", nickname = "호스트", remainingBudget = 300),
        )
    }

    @Nested
    inner class `참가 성공` {
        @Test
        fun `정원이 한 자리 남은 대기 방에는 마지막 자리까지 참가할 수 있다`() {
            val leader = cut.join(roomCode, "참가자")
            val leaders = leaderRepo.findByRoomId(roomId)

            assertThat(leader.nickname).isEqualTo("참가자")
            assertThat(leader.roomId).isEqualTo(roomId)
            assertThat(leader.remainingBudget).isEqualTo(300)
            assertThat(leaders).hasSize(2)
        }

        @Test
        fun `참가한 팀장의 잔여 예산은 방의 예산을 그대로 따른다`() {
            val leader = cut.join(roomCode, "예산참가자")

            assertThat(leader.remainingBudget).isEqualTo(300)
        }

        @Test
        fun `예산이 없는 방에 참가하면 팀장의 잔여 예산도 비워둔다`() {
            roomRepo.save(
                Room.createDraft(
                    code = roomCode,
                    hostId = "host",
                    teamCount = 2,
                    teamSize = 2,
                    draftOrderStrategy = DraftOrderStrategy.SNAKE,
                ).copy(roomId = roomId),
            )

            val leader = cut.join(roomCode, "드래프트참가자")

            assertThat(leader.remainingBudget).isNull()
        }

        @Test
        fun `legacy 드래프트 방의 stale budget은 무시하고 참가시킨다`() {
            val legacyRoomRepo = LegacyRoomRepository(legacyDraftRoom())
            cut = RoomJoinServiceImpl(RoomAggregateRepositoryImpl(legacyRoomRepo, playerRepo, leaderRepo, memberRepo, bidRepo), events)

            val leader = cut.join(roomCode, "드래프트참가자")

            assertThat(leader.remainingBudget).isNull()
        }
    }

    @Nested
    inner class `참가 불가` {
        @Test
        fun `존재하지 않는 방에 참가할 수 없다`() {
            assertThatThrownBy { cut.join("NOROOM", "참가자") }
                .isInstanceOf(RoomException.RoomNotFoundException::class.java)
        }

        @ParameterizedTest(name = "{0} 상태의 방에는 참가할 수 없다")
        @EnumSource(value = RoomStatus::class, names = ["WAITING"], mode = EnumSource.Mode.EXCLUDE)
        fun `WAITING이 아닌 상태의 방에는 참가할 수 없다`(status: RoomStatus) {
            roomRepo.save(
                Room.createAuction(
                    code = roomCode,
                    hostId = "host",
                    teamCount = 2,
                    teamSize = 2,
                    budget = 300,
                ).copy(
                    roomId = roomId,
                    status = status,
                ),
            )

            assertThatThrownBy { cut.join(roomCode, "참가자") }
                .isInstanceOf(IllegalStateException::class.java)
                .hasMessage("대기 중인 방에서만 참가할 수 있습니다")
        }

        @ParameterizedTest(name = "현재 팀장이 {0}명 더 있으면 참가할 수 없다")
        @ValueSource(ints = [1, 2])
        fun `방 정원이 가득 찼거나 초과된 상태면 참가할 수 없다`(additionalLeaders: Int) {
            repeat(additionalLeaders) { index ->
                leaderRepo.save(
                    RoomTeamLeader(
                        roomId = roomId,
                        teamLeaderId = "leader-${index + 2}",
                        nickname = "추가팀장${index + 1}",
                        remainingBudget = 300,
                    ),
                )
            }

            assertThatThrownBy { cut.join(roomCode, "세번째") }
                .isInstanceOf(IllegalStateException::class.java)
                .hasMessage("방이 가득 찼습니다")
        }
    }

    private fun legacyDraftRoom(): Room =
        Room.from(
            object : RoomModel {
                override val roomId = this@RoomJoinServiceTest.roomId
                override val code = this@RoomJoinServiceTest.roomCode
                override val hostId = "host"
                override val status = RoomStatus.WAITING
                override val mode = TeamBuildingMode.DRAFT
                override val teamCount = 2
                override val teamSize = 2
                override val budget = 300
                override val draftOrderStrategy = DraftOrderStrategy.SNAKE
                override val currentTurnIndex: Int? = null
                override val currentAuctionRound: Int? = null
                override val createdAt = java.time.Instant.parse("2025-01-01T00:00:00Z")
                override val updatedAt = java.time.Instant.parse("2025-01-01T00:00:00Z")
            },
        )

    private class LegacyRoomRepository(
        private var room: Room,
    ) : RoomRepository {
        override fun save(room: Room): Room {
            this.room = room
            return room
        }

        override fun findByCode(code: String): Room? = room.takeIf { it.code == code }

        override fun findById(roomId: Long): Room? = room.takeIf { it.roomId == roomId }
    }
}
