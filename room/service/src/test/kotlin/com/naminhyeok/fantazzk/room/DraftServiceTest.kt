package com.naminhyeok.fantazzk.room

import com.naminhyeok.fantazzk.room.exception.RoomException
import com.naminhyeok.fantazzk.room.support.InMemoryRoomPlayerRepository
import com.naminhyeok.fantazzk.room.support.InMemoryRoomRepository
import com.naminhyeok.fantazzk.room.support.InMemoryRoomTeamLeaderRepository
import com.naminhyeok.fantazzk.room.support.InMemoryRoomTeamMemberRepository
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class DraftServiceTest {
    private lateinit var roomRepo: InMemoryRoomRepository
    private lateinit var playerRepo: InMemoryRoomPlayerRepository
    private lateinit var leaderRepo: InMemoryRoomTeamLeaderRepository
    private lateinit var memberRepo: InMemoryRoomTeamMemberRepository
    private lateinit var cut: DraftService

    private lateinit var roomCode: String
    private var roomId: Long = 0L

    @BeforeEach
    fun setUp() {
        roomRepo = InMemoryRoomRepository()
        playerRepo = InMemoryRoomPlayerRepository()
        leaderRepo = InMemoryRoomTeamLeaderRepository()
        memberRepo = InMemoryRoomTeamMemberRepository()
        cut = DraftServiceImpl(roomRepo, leaderRepo, playerRepo, memberRepo)

        val room =
            roomRepo.save(
                Room(
                    code = "DRAFT1",
                    hostId = "host",
                    status = RoomStatus.IN_PROGRESS,
                    mode = TeamBuildingMode.DRAFT,
                    teamCount = 2,
                    teamSize = 2,
                    draftOrderStrategy = DraftOrderStrategy.SNAKE,
                    currentTurnIndex = 0,
                ),
            )
        roomCode = room.code
        roomId = room.roomId

        playerRepo.saveAll(
            listOf(
                RoomPlayer(roomId = roomId, name = "선수1", displayOrder = 0),
                RoomPlayer(roomId = roomId, name = "선수2", displayOrder = 1),
            ),
        )
        leaderRepo.save(RoomTeamLeader(roomId = roomId, teamLeaderId = "leader-A", nickname = "팀장A"))
        leaderRepo.save(RoomTeamLeader(roomId = roomId, teamLeaderId = "leader-B", nickname = "팀장B"))
    }

    @Nested
    inner class `픽 성공` {
        @Test
        fun `현재 턴의 팀장이 선수를 픽할 수 있다`() {
            val member = cut.pick(roomCode, "leader-A", "선수1")

            assertThat(member.teamLeaderId).isEqualTo("leader-A")
            assertThat(member.playerName).isEqualTo("선수1")
        }

        @Test
        fun `모든 픽이 완료되면 방이 완료된다`() {
            cut.pick(roomCode, "leader-A", "선수1")
            cut.pick(roomCode, "leader-B", "선수2")

            val room = roomRepo.findByCode(roomCode)!!
            assertThat(room.status).isEqualTo(RoomStatus.COMPLETED)
        }
    }

    @Nested
    inner class `픽 실패` {
        @Test
        fun `존재하지 않는 방에서 픽할 수 없다`() {
            assertThatThrownBy { cut.pick("NOROOM", "leader-A", "선수1") }
                .isInstanceOf(RoomException.RoomNotFoundException::class.java)
        }

        @Test
        fun `대기 중인 방에서는 픽할 수 없다`() {
            roomRepo.save(
                Room(
                    roomId = roomId,
                    code = roomCode,
                    hostId = "host",
                    status = RoomStatus.WAITING,
                    mode = TeamBuildingMode.DRAFT,
                    teamCount = 2,
                    teamSize = 2,
                    draftOrderStrategy = DraftOrderStrategy.SNAKE,
                    currentTurnIndex = 0,
                ),
            )

            assertThatThrownBy { cut.pick(roomCode, "leader-A", "선수1") }
                .isInstanceOf(IllegalStateException::class.java)
        }

        @Test
        fun `경매 모드에서는 픽할 수 없다`() {
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
                ),
            )

            assertThatThrownBy { cut.pick(roomCode, "leader-A", "선수1") }
                .isInstanceOf(IllegalStateException::class.java)
        }

        @Test
        fun `자신의 턴이 아니면 픽할 수 없다`() {
            assertThatThrownBy { cut.pick(roomCode, "leader-B", "선수1") }
                .isInstanceOf(IllegalStateException::class.java)
                .hasMessageContaining("현재 턴이 아닙니다")
        }

        @Test
        fun `존재하지 않는 선수는 픽할 수 없다`() {
            assertThatThrownBy { cut.pick(roomCode, "leader-A", "없는선수") }
                .isInstanceOf(IllegalArgumentException::class.java)
        }
    }

    @Nested
    inner class `픽 순서 전략` {
        @Test
        fun `SNAKE 전략은 홀수 라운드에서 순서가 뒤집힌다`() {
            val order = DraftServiceImpl.generatePickOrder(listOf("A", "B"), DraftOrderStrategy.SNAKE, 2)
            assertThat(order).containsExactly("A", "B", "B", "A")
        }

        @Test
        fun `FIXED 전략은 매 라운드 동일 순서를 유지한다`() {
            val order = DraftServiceImpl.generatePickOrder(listOf("A", "B"), DraftOrderStrategy.FIXED, 2)
            assertThat(order).containsExactly("A", "B", "A", "B")
        }
    }
}
