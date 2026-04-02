package com.naminhyeok.fantazzk.room

import com.naminhyeok.fantazzk.room.application.RoomFinder
import com.naminhyeok.fantazzk.room.application.RoomFinderImpl
import com.naminhyeok.fantazzk.room.exception.RoomException
import com.naminhyeok.fantazzk.room.support.InMemoryRoomRepository
import com.naminhyeok.fantazzk.room.support.InMemoryRoomTeamLeaderRepository
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class RoomFinderTest {
    private lateinit var roomRepo: InMemoryRoomRepository
    private lateinit var leaderRepo: InMemoryRoomTeamLeaderRepository
    private lateinit var cut: RoomFinder
    private var roomId: Long = 0L

    @BeforeEach
    fun setUp() {
        leaderRepo = InMemoryRoomTeamLeaderRepository()
        roomRepo = InMemoryRoomRepository(roomTeamLeaderRepository = leaderRepo)
        cut = RoomFinderImpl(roomRepo)

        roomId =
            roomRepo.save(
                Room(
                    code = "LOOK01",
                    hostId = "host",
                    status = RoomStatus.WAITING,
                    mode = TeamBuildingMode.AUCTION,
                    teamCount = 2,
                    teamSize = 2,
                    budget = 300,
                ),
            ).roomId
    }

    @Test
    fun `코드로 방 aggregate 를 조회할 때 팀장 정보까지 hydrate 한다`() {
        leaderRepo.save(
            RoomTeamLeader(
                roomId = roomId,
                teamLeaderId = "leader-1",
                nickname = "참가자",
                remainingBudget = 300,
            ),
        )

        val room = cut.get("LOOK01")

        assertThat(room.code).isEqualTo("LOOK01")
        assertThat(room.leaders).hasSize(1)
        assertThat(room.leaders.single().nickname).isEqualTo("참가자")
    }

    @Test
    fun `존재하지 않는 코드로 조회하면 예외가 발생한다`() {
        assertThatThrownBy { cut.get("NOCODE") }
            .isInstanceOf(RoomException.RoomNotFoundException::class.java)
    }
}
