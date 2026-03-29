package com.naminhyeok.fantazzk.room

import com.naminhyeok.fantazzk.room.exception.RoomException
import com.naminhyeok.fantazzk.room.support.InMemoryRoomRepository
import com.naminhyeok.fantazzk.room.support.InMemoryRoomTeamLeaderRepository
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.EnumSource

class RoomJoinServiceTest {
    private lateinit var roomRepo: InMemoryRoomRepository
    private lateinit var leaderRepo: InMemoryRoomTeamLeaderRepository
    private lateinit var cut: RoomJoinService

    private lateinit var roomCode: String
    private var roomId: Long = 0L

    @BeforeEach
    fun setUp() {
        roomRepo = InMemoryRoomRepository()
        leaderRepo = InMemoryRoomTeamLeaderRepository()
        cut = RoomJoinServiceImpl(roomRepo, leaderRepo)

        val room =
            roomRepo.save(
                Room(
                    code = "JOIN01",
                    hostId = "host",
                    status = RoomStatus.WAITING,
                    mode = TeamBuildingMode.AUCTION,
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
        fun `대기 중인 방에 참가하면 팀장으로 등록된다`() {
            val leader = cut.join(roomCode, "참가자")

            assertThat(leader.nickname).isEqualTo("참가자")
            assertThat(leader.roomId).isEqualTo(roomId)
            assertThat(leader.remainingBudget).isEqualTo(300)
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
                Room(
                    roomId = roomId,
                    code = roomCode,
                    hostId = "host",
                    status = status,
                    mode = TeamBuildingMode.AUCTION,
                    teamCount = 2,
                    teamSize = 2,
                ),
            )

            assertThatThrownBy { cut.join(roomCode, "참가자") }
                .isInstanceOf(IllegalStateException::class.java)
        }

        @Test
        fun `방 정원이 가득 차면 참가할 수 없다`() {
            leaderRepo.save(
                RoomTeamLeader(roomId = roomId, teamLeaderId = "leader-2", nickname = "두번째", remainingBudget = 300),
            )

            assertThatThrownBy { cut.join(roomCode, "세번째") }
                .isInstanceOf(IllegalStateException::class.java)
        }
    }
}
