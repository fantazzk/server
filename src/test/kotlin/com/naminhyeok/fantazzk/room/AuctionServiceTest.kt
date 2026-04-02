package com.naminhyeok.fantazzk.room

import com.naminhyeok.fantazzk.room.application.AuctionService
import com.naminhyeok.fantazzk.room.application.AuctionServiceImpl
import com.naminhyeok.fantazzk.room.exception.RoomException
import com.naminhyeok.fantazzk.room.support.InMemoryRoomBidRepository
import com.naminhyeok.fantazzk.room.support.InMemoryRoomPlayerRepository
import com.naminhyeok.fantazzk.room.support.InMemoryRoomRepository
import com.naminhyeok.fantazzk.room.support.InMemoryRoomTeamLeaderRepository
import com.naminhyeok.fantazzk.room.support.InMemoryRoomTeamMemberRepository
import io.mockk.mockk
import io.mockk.verify
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.springframework.context.ApplicationEventPublisher

class AuctionServiceTest {
    private lateinit var roomRepo: InMemoryRoomRepository
    private lateinit var playerRepo: InMemoryRoomPlayerRepository
    private lateinit var leaderRepo: InMemoryRoomTeamLeaderRepository
    private lateinit var memberRepo: InMemoryRoomTeamMemberRepository
    private lateinit var bidRepo: InMemoryRoomBidRepository
    private lateinit var events: ApplicationEventPublisher
    private lateinit var cut: AuctionService

    private lateinit var roomCode: String
    private var roomId: Long = 0L

    @BeforeEach
    fun setUp() {
        playerRepo = InMemoryRoomPlayerRepository()
        leaderRepo = InMemoryRoomTeamLeaderRepository()
        memberRepo = InMemoryRoomTeamMemberRepository()
        bidRepo = InMemoryRoomBidRepository()
        roomRepo = InMemoryRoomRepository(playerRepo, leaderRepo, memberRepo, bidRepo)
        events = mockk(relaxed = true)
        cut = AuctionServiceImpl(roomRepo, events)

        val room =
            roomRepo.save(
                Room(
                    code = "TEST01",
                    hostId = "host",
                    status = RoomStatus.IN_PROGRESS,
                    mode = TeamBuildingMode.AUCTION,
                    teamCount = 2,
                    teamSize = 2,
                    budget = 300,
                    currentAuctionRound = 1,
                ),
            )
        roomCode = room.code
        roomId = room.roomId

        playerRepo.saveAll(
            listOf(
                RoomPlayer(roomId = roomId, name = "선수1", displayOrder = 0),
                RoomPlayer(roomId = roomId, name = "선수2", displayOrder = 1),
                RoomPlayer(roomId = roomId, name = "선수3", displayOrder = 2),
            ),
        )
        leaderRepo.save(RoomTeamLeader(roomId = roomId, teamLeaderId = "leader-A", nickname = "팀장A", remainingBudget = 300))
        leaderRepo.save(RoomTeamLeader(roomId = roomId, teamLeaderId = "leader-B", nickname = "팀장B", remainingBudget = 300))
    }

    @Nested
    inner class `입찰 성공` {
        @Test
        fun `입찰하면 입찰 기록이 저장된다`() {
            val bid = cut.placeBid(roomCode, "leader-A", 100)

            assertThat(bid.amount).isEqualTo(100)
            assertThat(bid.teamLeaderId).isEqualTo("leader-A")
        }
    }

    @Nested
    inner class `입찰 실패` {
        @Test
        fun `존재하지 않는 방에 입찰할 수 없다`() {
            assertThatThrownBy { cut.placeBid("NOROOM", "leader-A", 100) }
                .isInstanceOf(RoomException.RoomNotFoundException::class.java)
        }

        @Test
        fun `대기 중인 방에는 입찰할 수 없다`() {
            roomRepo.save(
                Room(
                    roomId = roomId,
                    code = roomCode,
                    hostId = "host",
                    status = RoomStatus.WAITING,
                    mode = TeamBuildingMode.AUCTION,
                    teamCount = 2,
                    teamSize = 2,
                    budget = 300,
                ),
            )

            assertThatThrownBy { cut.placeBid(roomCode, "leader-A", 100) }
                .isInstanceOf(IllegalStateException::class.java)
        }

        @Test
        fun `드래프트 모드에서는 입찰할 수 없다`() {
            roomRepo.save(
                Room(
                    roomId = roomId,
                    code = roomCode,
                    hostId = "host",
                    status = RoomStatus.IN_PROGRESS,
                    mode = TeamBuildingMode.DRAFT,
                    teamCount = 2,
                    teamSize = 2,
                    draftOrderStrategy = DraftOrderStrategy.SNAKE,
                ),
            )

            assertThatThrownBy { cut.placeBid(roomCode, "leader-A", 100) }
                .isInstanceOf(IllegalStateException::class.java)
        }

        @Test
        fun `존재하지 않는 팀장은 입찰할 수 없다`() {
            assertThatThrownBy { cut.placeBid(roomCode, "unknown-leader", 100) }
                .isInstanceOf(RoomException.TeamLeaderNotFoundException::class.java)
        }

        @Test
        fun `예산을 초과하여 입찰할 수 없다`() {
            assertThatThrownBy { cut.placeBid(roomCode, "leader-A", 301) }
                .isInstanceOf(IllegalArgumentException::class.java)
        }

        @Test
        fun `현재 최고가 이하로 입찰할 수 없다`() {
            cut.placeBid(roomCode, "leader-A", 100)

            assertThatThrownBy { cut.placeBid(roomCode, "leader-B", 100) }
                .isInstanceOf(IllegalArgumentException::class.java)
                .hasMessageContaining("현재 최고가보다 높아야 합니다")
        }

        @Test
        fun `정산으로 다음 라운드가 된 뒤에는 이전 라운드 최고가와 무관하게 다시 입찰한다`() {
            cut.placeBid(roomCode, "leader-A", 100)
            cut.settle(roomCode)

            val nextBid = cut.placeBid(roomCode, "leader-B", 100)

            assertThat(nextBid.round).isEqualTo(2)
            assertThat(nextBid.amount).isEqualTo(100)
        }

        @Test
        fun `현재 경매 라운드가 없으면 입찰 기록을 저장하지 않는다`() {
            roomRepo.save(
                Room(
                    roomId = roomId,
                    code = roomCode,
                    hostId = "host",
                    status = RoomStatus.IN_PROGRESS,
                    mode = TeamBuildingMode.AUCTION,
                    teamCount = 2,
                    teamSize = 2,
                    budget = 300,
                    currentAuctionRound = null,
                ),
            )

            assertThatThrownBy { cut.placeBid(roomCode, "leader-A", 100) }
                .isInstanceOf(IllegalArgumentException::class.java)
                .hasMessageContaining("현재 경매 라운드가 없습니다")

            assertThat(bidRepo.findHighestByRoomIdAndRound(roomId, 1)).isNull()
        }
    }

    @Nested
    inner class `정산 - 낙찰` {
        @Test
        fun `낙찰 시 선수가 팀에 배정되고 예산이 차감된다`() {
            cut.placeBid(roomCode, "leader-A", 100)
            cut.placeBid(roomCode, "leader-B", 150)
            val result = cut.settle(roomCode)

            assertThat(result.playerName).isEqualTo("선수1")
            assertThat(result.outcome).isEqualTo(AuctionOutcome.SOLD)

            val members = memberRepo.findByRoomIdAndTeamLeaderId(roomId, "leader-B")
            assertThat(members).hasSize(1)
            assertThat(members.first().playerName).isEqualTo("선수1")

            val winner = leaderRepo.findByRoomIdAndTeamLeaderId(roomId, "leader-B")!!
            assertThat(winner.remainingBudget).isEqualTo(150)

            val assignedPlayer = playerRepo.findByRoomId(roomId).first { it.name == "선수1" }
            assertThat(assignedPlayer.status).isEqualTo(PlayerStatus.ASSIGNED)
        }

        @Test
        fun `정산 성공 시 AuctionSettled 이벤트를 발행한다`() {
            cut.placeBid(roomCode, "leader-A", 100)

            cut.settle(roomCode)

            verify {
                events.publishEvent(
                    match<AuctionSettled> {
                        it.roomId == roomId &&
                            it.code == roomCode &&
                            it.playerName == "선수1" &&
                            it.outcome == AuctionOutcome.SOLD &&
                            it.leaders.any { leader -> leader.teamLeaderId == "leader-A" && leader.remainingBudget == 200 } &&
                            it.leaders.any { leader -> leader.teamLeaderId == "leader-B" && leader.remainingBudget == 300 }
                    },
                )
            }
        }

        @Test
        fun `모든 팀 정원이 채워지면 방이 완료된다`() {
            cut.placeBid(roomCode, "leader-A", 100)
            cut.settle(roomCode)

            cut.placeBid(roomCode, "leader-B", 100)
            cut.settle(roomCode)

            val room = roomRepo.findByCode(roomCode)!!
            assertThat(room.status).isEqualTo(RoomStatus.COMPLETED)

            verify {
                events.publishEvent(
                    match<RoomCompleted> {
                        it.roomId == roomId &&
                            it.code == roomCode &&
                            it.status == RoomStatus.COMPLETED &&
                            it.mode == RoomStarted.Mode.AUCTION
                    },
                )
            }
        }
    }

    @Nested
    inner class `정산 - 유찰` {
        @Test
        fun `유찰 시 선수가 풀 뒤로 이동한다`() {
            val result = cut.settle(roomCode)

            assertThat(result.outcome).isEqualTo(AuctionOutcome.PASSED)
            assertThat(result.playerName).isEqualTo("선수1")

            val nextTarget = playerRepo.findFirstAvailable(roomId)
            assertThat(nextTarget?.name).isEqualTo("선수2")

            val movedPlayer = playerRepo.findByRoomId(roomId).first { it.name == "선수1" }
            assertThat(movedPlayer.displayOrder).isEqualTo(3)
            assertThat(movedPlayer.status).isEqualTo(PlayerStatus.AVAILABLE)
        }

        @Test
        fun `유찰 후 다음 경매에서 입찰이 이전 라운드와 혼동되지 않는다`() {
            cut.settle(roomCode)

            cut.placeBid(roomCode, "leader-A", 100)
            val result = cut.settle(roomCode)

            assertThat(result.playerName).isEqualTo("선수2")
            assertThat(result.outcome).isEqualTo(AuctionOutcome.SOLD)
        }
    }

    @Nested
    inner class `정산 실패` {
        @Test
        fun `존재하지 않는 방은 정산할 수 없다`() {
            assertThatThrownBy { cut.settle("NOROOM") }
                .isInstanceOf(RoomException.RoomNotFoundException::class.java)
        }

        @Test
        fun `경매할 선수가 없으면 정산할 수 없다`() {
            playerRepo.findByRoomId(roomId).forEach { player ->
                playerRepo.save(player.assign())
            }

            assertThatThrownBy { cut.settle(roomCode) }
                .isInstanceOf(IllegalArgumentException::class.java)
        }

        @Test
        fun `현재 경매 라운드가 없으면 정산 전에 어떤 상태도 변경하지 않는다`() {
            roomRepo.save(
                Room(
                    roomId = roomId,
                    code = roomCode,
                    hostId = "host",
                    status = RoomStatus.IN_PROGRESS,
                    mode = TeamBuildingMode.AUCTION,
                    teamCount = 2,
                    teamSize = 2,
                    budget = 300,
                    currentAuctionRound = null,
                ),
            )
            bidRepo.save(RoomBid(roomId = roomId, round = 1, teamLeaderId = "leader-A", amount = 100))

            assertThatThrownBy { cut.settle(roomCode) }
                .isInstanceOf(IllegalArgumentException::class.java)
                .hasMessageContaining("현재 경매 라운드가 없습니다")

            val player = playerRepo.findByRoomId(roomId).first { it.name == "선수1" }
            assertThat(player.status).isEqualTo(PlayerStatus.AVAILABLE)

            val leader = leaderRepo.findByRoomIdAndTeamLeaderId(roomId, "leader-A")!!
            assertThat(leader.remainingBudget).isEqualTo(300)
            assertThat(memberRepo.countByRoomId(roomId)).isZero()
            assertThat(roomRepo.findByCode(roomCode)?.currentAuctionRound).isNull()
        }
    }
}
