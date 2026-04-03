@file:Suppress("ktlint:standard:no-wildcard-imports")

package com.naminhyeok.fantazzk.room

import com.naminhyeok.fantazzk.room.application.PickDraft
import com.naminhyeok.fantazzk.room.domain.*
import com.naminhyeok.fantazzk.room.exception.RoomException
import com.naminhyeok.fantazzk.room.support.InMemoryRoomRepository
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class DraftServiceTest {
    private lateinit var roomRepo: InMemoryRoomRepository
    private lateinit var cut: PickDraft

    private lateinit var roomCode: String
    private var roomId: Long = 0L

    @BeforeEach
    fun setUp() {
        roomRepo = InMemoryRoomRepository()
        cut = PickDraft(roomRepo)

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
                    players =
                        listOf(
                            RoomPlayer(name = "선수1", displayOrder = 0),
                            RoomPlayer(name = "선수2", displayOrder = 1),
                        ),
                    leaders =
                        listOf(
                            RoomTeamLeader(teamLeaderId = "leader-A", nickname = "팀장A"),
                            RoomTeamLeader(teamLeaderId = "leader-B", nickname = "팀장B"),
                        ),
                ),
            )
        roomCode = room.code
        roomId = room.roomId
    }

    @Nested
    inner class `픽 성공` {
        @Test
        fun `현재 턴의 팀장이 선수를 픽할 수 있다`() {
            val member = cut.pick(roomCode, "leader-A", "선수1")

            assertThat(member.teamLeaderId).isEqualTo("leader-A")
            assertThat(member.playerName).isEqualTo("선수1")
            assertThat(currentRoom().players.first { it.name == "선수1" }.status).isEqualTo(PlayerStatus.ASSIGNED)
        }

        @Test
        fun `모든 픽이 완료되면 방이 완료된다`() {
            cut.pick(roomCode, "leader-A", "선수1")
            cut.pick(roomCode, "leader-B", "선수2")

            val room = currentRoom()
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
            persistRoom { room -> room.copy(status = RoomStatus.WAITING) }

            assertThatThrownBy { cut.pick(roomCode, "leader-A", "선수1") }
                .isInstanceOf(IllegalStateException::class.java)
        }

        @Test
        fun `경매 모드에서는 픽할 수 없다`() {
            persistRoom {
                Room(
                    roomId = roomId,
                    code = roomCode,
                    hostId = "host",
                    status = RoomStatus.IN_PROGRESS,
                    mode = TeamBuildingMode.AUCTION,
                    teamCount = 2,
                    teamSize = 2,
                    budget = 300,
                    players = it.players,
                    leaders = it.leaders,
                    members = it.members,
                )
            }

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

        @Test
        fun `현재 드래프트 턴이 없으면 픽 전에 어떤 상태도 변경하지 않는다`() {
            persistRoom { room -> room.copy(currentTurnIndex = null) }

            assertThatThrownBy { cut.pick(roomCode, "leader-A", "선수1") }
                .isInstanceOf(IllegalArgumentException::class.java)
                .hasMessageContaining("현재 드래프트 턴이 없습니다")

            val room = currentRoom()
            assertThat(room.players.first { it.name == "선수1" }.status).isEqualTo(PlayerStatus.AVAILABLE)
            assertThat(room.members).isEmpty()
            assertThat(room.currentTurnIndex).isNull()
        }
    }

    @Nested
    inner class `픽 순서 전략` {
        @Test
        fun `SNAKE 전략은 홀수 라운드에서 순서가 뒤집힌다`() {
            val order = DraftBoard(listOf("A", "B"), DraftOrderStrategy.SNAKE, 2).pickOrder()
            assertThat(order).containsExactly("A", "B", "B", "A")
        }

        @Test
        fun `FIXED 전략은 매 라운드 동일 순서를 유지한다`() {
            val order = DraftBoard(listOf("A", "B"), DraftOrderStrategy.FIXED, 2).pickOrder()
            assertThat(order).containsExactly("A", "B", "A", "B")
        }
    }

    private fun currentRoom(): Room = roomRepo.findByCode(roomCode)!!

    private fun persistRoom(transform: (Room) -> Room) {
        roomRepo.save(transform(currentRoom()))
    }
}
