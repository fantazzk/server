@file:Suppress("ktlint:standard:no-wildcard-imports")

package com.naminhyeok.fantazzk.room

import com.naminhyeok.fantazzk.room.application.PlaceBid
import com.naminhyeok.fantazzk.room.application.SettleAuction
import com.naminhyeok.fantazzk.room.domain.*
import com.naminhyeok.fantazzk.room.exception.RoomException
import com.naminhyeok.fantazzk.room.support.InMemoryRoomRepository
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class AuctionServiceTest {
    private lateinit var roomRepo: InMemoryRoomRepository
    private lateinit var placeBid: PlaceBid
    private lateinit var settleAuction: SettleAuction

    private lateinit var roomCode: String
    private var roomId: Long = 0L

    @BeforeEach
    fun setUp() {
        roomRepo = InMemoryRoomRepository()
        placeBid = PlaceBid(roomRepo)
        settleAuction = SettleAuction(roomRepo)

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
                    players =
                        listOf(
                            RoomPlayer("선수1", 0),
                            RoomPlayer("선수2", 1),
                            RoomPlayer("선수3", 2),
                        ),
                    leaders =
                        listOf(
                            RoomTeamLeader(teamLeaderId = "leader-A", nickname = "팀장A", remainingBudget = 300),
                            RoomTeamLeader(teamLeaderId = "leader-B", nickname = "팀장B", remainingBudget = 300),
                        ),
                ),
            )
        roomCode = room.code
        roomId = room.roomId
    }

    @Nested
    inner class `입찰 성공` {
        @Test
        fun `입찰하면 입찰 기록이 저장된다`() {
            val bid = placeBid.place(roomCode, "leader-A", 100)

            assertThat(bid.amount).isEqualTo(100)
            assertThat(bid.teamLeaderId).isEqualTo("leader-A")
        }
    }

    @Nested
    inner class `입찰 실패` {
        @Test
        fun `존재하지 않는 방에 입찰할 수 없다`() {
            assertThatThrownBy { placeBid.place("NOROOM", "leader-A", 100) }
                .isInstanceOf(RoomException.RoomNotFoundException::class.java)
        }

        @Test
        fun `대기 중인 방에는 입찰할 수 없다`() {
            persistRoom { room -> room.copy(status = RoomStatus.WAITING) }

            assertThatThrownBy { placeBid.place(roomCode, "leader-A", 100) }
                .isInstanceOf(IllegalStateException::class.java)
        }

        @Test
        fun `드래프트 모드에서는 입찰할 수 없다`() {
            persistRoom {
                Room(
                    roomId = roomId,
                    code = roomCode,
                    hostId = "host",
                    status = RoomStatus.IN_PROGRESS,
                    mode = TeamBuildingMode.DRAFT,
                    teamCount = 2,
                    teamSize = 2,
                    draftOrderStrategy = DraftOrderStrategy.SNAKE,
                    players = it.players,
                    leaders = it.leaders,
                    members = it.members,
                )
            }

            assertThatThrownBy { placeBid.place(roomCode, "leader-A", 100) }
                .isInstanceOf(IllegalStateException::class.java)
        }

        @Test
        fun `존재하지 않는 팀장은 입찰할 수 없다`() {
            assertThatThrownBy { placeBid.place(roomCode, "unknown-leader", 100) }
                .isInstanceOf(RoomException.TeamLeaderNotFoundException::class.java)
        }

        @Test
        fun `예산을 초과하여 입찰할 수 없다`() {
            assertThatThrownBy { placeBid.place(roomCode, "leader-A", 301) }
                .isInstanceOf(IllegalArgumentException::class.java)
        }

        @Test
        fun `현재 최고가 이하로 입찰할 수 없다`() {
            placeBid.place(roomCode, "leader-A", 100)

            assertThatThrownBy { placeBid.place(roomCode, "leader-B", 100) }
                .isInstanceOf(IllegalArgumentException::class.java)
                .hasMessageContaining("현재 최고가보다 높아야 합니다")
        }

        @Test
        fun `정산으로 다음 라운드가 된 뒤에는 이전 라운드 최고가와 무관하게 다시 입찰한다`() {
            placeBid.place(roomCode, "leader-A", 100)
            settleAuction.settle(roomCode)

            val nextBid = placeBid.place(roomCode, "leader-B", 100)

            assertThat(nextBid.round).isEqualTo(2)
            assertThat(nextBid.amount).isEqualTo(100)
        }

        @Test
        fun `현재 경매 라운드가 없으면 입찰 기록을 저장하지 않는다`() {
            persistRoom { room -> room.copy(currentAuctionRound = null, bids = emptyList()) }

            assertThatThrownBy { placeBid.place(roomCode, "leader-A", 100) }
                .isInstanceOf(IllegalArgumentException::class.java)
                .hasMessageContaining("현재 경매 라운드가 없습니다")

            assertThat(currentRoom().bidHistory()).isEmpty()
        }
    }

    @Nested
    inner class `정산 - 낙찰` {
        @Test
        fun `낙찰 시 선수가 팀에 배정되고 예산이 차감된다`() {
            placeBid.place(roomCode, "leader-A", 100)
            placeBid.place(roomCode, "leader-B", 150)
            val result = settleAuction.settle(roomCode)

            assertThat(result.playerName).isEqualTo("선수1")
            assertThat(result.outcome).isEqualTo(AuctionOutcome.SOLD)

            val room = currentRoom()
            assertThat(room.members.filter { it.teamLeaderId == "leader-B" }).hasSize(1)
            assertThat(room.members.first { it.teamLeaderId == "leader-B" }.playerName).isEqualTo("선수1")
            assertThat(room.leaders.first { it.teamLeaderId == "leader-B" }.remainingBudget).isEqualTo(150)
            assertThat(room.players.first { it.name == "선수1" }.status).isEqualTo(PlayerStatus.ASSIGNED)
        }

        @Test
        fun `모든 팀 정원이 채워지면 방이 완료된다`() {
            placeBid.place(roomCode, "leader-A", 100)
            settleAuction.settle(roomCode)

            placeBid.place(roomCode, "leader-B", 100)
            settleAuction.settle(roomCode)

            val room = currentRoom()
            assertThat(room.status).isEqualTo(RoomStatus.COMPLETED)
        }
    }

    @Nested
    inner class `정산 - 유찰` {
        @Test
        fun `유찰 시 선수가 풀 뒤로 이동한다`() {
            val result = settleAuction.settle(roomCode)

            assertThat(result.outcome).isEqualTo(AuctionOutcome.PASSED)
            assertThat(result.playerName).isEqualTo("선수1")

            val room = currentRoom()
            assertThat(room.players.filter { it.status == PlayerStatus.AVAILABLE }.minByOrNull { it.displayOrder }?.name).isEqualTo("선수2")
            assertThat(room.players.first { it.name == "선수1" }.displayOrder).isEqualTo(3)
            assertThat(room.players.first { it.name == "선수1" }.status).isEqualTo(PlayerStatus.AVAILABLE)
        }

        @Test
        fun `유찰 후 다음 경매에서 입찰이 이전 라운드와 혼동되지 않는다`() {
            settleAuction.settle(roomCode)

            placeBid.place(roomCode, "leader-A", 100)
            val result = settleAuction.settle(roomCode)

            assertThat(result.playerName).isEqualTo("선수2")
            assertThat(result.outcome).isEqualTo(AuctionOutcome.SOLD)
        }
    }

    @Nested
    inner class `정산 실패` {
        @Test
        fun `존재하지 않는 방은 정산할 수 없다`() {
            assertThatThrownBy { settleAuction.settle("NOROOM") }
                .isInstanceOf(RoomException.RoomNotFoundException::class.java)
        }

        @Test
        fun `경매할 선수가 없으면 정산할 수 없다`() {
            persistRoom { room ->
                room.copy(
                    players =
                        room.players.map {
                            it.copy(
                                it.roomPlayerId,
                                it.roomId,
                                it.name,
                                PlayerStatus.ASSIGNED,
                                it.displayOrder,
                                it.createdAt,
                                it.updatedAt,
                            )
                        },
                )
            }

            assertThatThrownBy { settleAuction.settle(roomCode) }
                .isInstanceOf(IllegalArgumentException::class.java)
        }

        @Test
        fun `현재 경매 라운드가 없으면 정산 전에 어떤 상태도 변경하지 않는다`() {
            persistRoom {
                it.copy(
                    currentAuctionRound = null,
                    bids = listOf(RoomBid(roomId, 1, "leader-A", 100, java.time.Instant.now(), java.time.Instant.now())),
                )
            }

            assertThatThrownBy { settleAuction.settle(roomCode) }
                .isInstanceOf(IllegalArgumentException::class.java)
                .hasMessageContaining("현재 경매 라운드가 없습니다")

            val room = currentRoom()
            assertThat(room.players.first { it.name == "선수1" }.status).isEqualTo(PlayerStatus.AVAILABLE)
            assertThat(room.leaders.first { it.teamLeaderId == "leader-A" }.remainingBudget).isEqualTo(300)
            assertThat(room.members).isEmpty()
            assertThat(room.currentAuctionRound).isNull()
        }
    }

    private fun currentRoom(): Room = roomRepo.findByCode(roomCode)!!

    private fun persistRoom(transform: (Room) -> Room) {
        roomRepo.save(transform(currentRoom()))
    }
}
