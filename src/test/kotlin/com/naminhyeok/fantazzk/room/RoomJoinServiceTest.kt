@file:Suppress("ktlint:standard:no-wildcard-imports")

package com.naminhyeok.fantazzk.room

import com.naminhyeok.fantazzk.room.application.JoinRoom
import com.naminhyeok.fantazzk.room.domain.*
import com.naminhyeok.fantazzk.room.exception.RoomException
import com.naminhyeok.fantazzk.room.support.copyRoom
import com.naminhyeok.fantazzk.room.support.InMemoryRoomRepository
import com.naminhyeok.fantazzk.room.support.leaderFixture
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.EnumSource
import org.junit.jupiter.params.provider.ValueSource

class RoomJoinServiceTest {
    private lateinit var roomRepo: InMemoryRoomRepository
    private lateinit var cut: JoinRoom

    private lateinit var roomCode: String
    private lateinit var roomId: RoomId

    @BeforeEach
    fun setUp() {
        roomRepo = InMemoryRoomRepository()
        cut = JoinRoom(roomRepo)

        val room =
            roomRepo.save(
                Room.createAuction(
                    "JOIN01",
                    "host",
                    2,
                    2,
                    300,
                ),
            )
        roomCode = room.code
        roomId = room.roomId

        persistRoom {
            copyRoom(
                it,
                leaders = listOf(leaderFixture(roomId = roomId, teamLeaderId = "host", nickname = "호스트", remainingBudget = 300)),
            )
        }
    }

    @Nested
    inner class `참가 성공` {
        @Test
        fun `정원이 한 자리 남은 대기 방에는 마지막 자리까지 참가할 수 있다`() {
            val leader = cut.join(roomCode, "참가자")
            val leaders = currentRoom().leaders

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
            persistRoom {
                Room.createDraft(
                    roomCode,
                    "host",
                    2,
                    2,
                    DraftOrderStrategy.SNAKE,
                ).let {
                    copyRoom(
                        it,
                    roomId = roomId,
                    leaders = currentRoom().leaders,
                    )
                }
            }

            val leader = cut.join(roomCode, "드래프트참가자")

            assertThat(nullable(leader.remainingBudget)).isNull()
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
            persistRoom {
                Room.createAuction(
                    roomCode,
                    "host",
                    2,
                    2,
                    300,
                ).let {
                    copyRoom(
                        it,
                    roomId = roomId,
                    status = status,
                    leaders = currentRoom().leaders,
                    )
                }
            }

            assertThatThrownBy { cut.join(roomCode, "참가자") }
                .isInstanceOf(IllegalStateException::class.java)
                .hasMessage("대기 중인 방에서만 참가할 수 있습니다")
        }

        @ParameterizedTest(name = "현재 팀장이 {0}명 더 있으면 참가할 수 없다")
        @ValueSource(ints = [1, 2])
        fun `방 정원이 가득 찼거나 초과된 상태면 참가할 수 없다`(additionalLeaders: Int) {
            repeat(additionalLeaders) { index ->
                persistRoom { room ->
                    copyRoom(
                        room,
                        leaders =
                            room.leaders +
                                leaderFixture(
                                    roomId = roomId,
                                    teamLeaderId = "leader-${index + 2}",
                                    nickname = "추가팀장${index + 1}",
                                    remainingBudget = 300,
                                ),
                    )
                }
            }

            assertThatThrownBy { cut.join(roomCode, "세번째") }
                .isInstanceOf(IllegalStateException::class.java)
                .hasMessage("방이 가득 찼습니다")
        }
    }

    private fun currentRoom(): Room = roomRepo.findByCode(roomCode)!!

    private fun persistRoom(transform: (Room) -> Room) {
        roomRepo.save(transform(currentRoom()))
    }
}
