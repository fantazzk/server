package com.naminhyeok.fantazzk.room.query

import com.naminhyeok.fantazzk.room.LeaderSnapshot
import com.naminhyeok.fantazzk.room.RoomCreated
import com.naminhyeok.fantazzk.room.RoomJoined
import com.naminhyeok.fantazzk.room.RoomStatus
import com.naminhyeok.fantazzk.room.support.InMemoryRoomRepository
import com.naminhyeok.fantazzk.room.support.InMemoryRoomTeamLeaderRepository
import io.mockk.mockk
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class RoomProjectionUpdaterTest {
    private lateinit var writer: InMemoryRoomProjectionWriter
    private lateinit var roomRepo: InMemoryRoomRepository
    private lateinit var leaderRepo: InMemoryRoomTeamLeaderRepository
    private lateinit var cut: RoomProjectionUpdater

    @BeforeEach
    fun setUp() {
        writer = InMemoryRoomProjectionWriter()
        roomRepo = InMemoryRoomRepository()
        leaderRepo = InMemoryRoomTeamLeaderRepository()
        cut = RoomProjectionUpdater(writer, roomRepo, leaderRepo)
    }

    @Test
    fun `RoomCreated event로 room view와 host leader projection을 만든다`() {
        cut.on(
            RoomCreated(
                roomId = 1L,
                code = "ROOM01",
                status = RoomStatus.WAITING,
                hostLeader = LeaderSnapshot(teamLeaderId = "host", nickname = "호스트", remainingBudget = 300),
            ),
        )

        assertThat(writer.roomViews.values.single().code).isEqualTo("ROOM01")
        val leader = writer.leaders.values.single()
        assertThat(leader.teamLeaderId).isEqualTo("host")
        assertThat(leader.nickname).isEqualTo("호스트")
    }

    @Test
    fun `RoomJoined event로 leader projection을 추가한다`() {
        cut.on(
            RoomJoined(
                roomId = 1L,
                code = "ROOM01",
                leader = LeaderSnapshot(teamLeaderId = "guest", nickname = "게스트", remainingBudget = 300),
            ),
        )

        val leaders = writer.leaders.values.toList()
        val leader = leaders.single()
        assertThat(leader.teamLeaderId).isEqualTo("guest")
        assertThat(leader.nickname).isEqualTo("게스트")
    }

    private class InMemoryRoomProjectionWriter : RoomProjectionWriter(mockk()) {
        val roomViews = linkedMapOf<Long, RoomViewEntity>()
        val leaders = linkedMapOf<Pair<Long, String>, TeamLeaderViewEntity>()
        private var seq = 1L

        override fun upsertRoom(
            roomId: Long,
            code: String,
            status: com.naminhyeok.fantazzk.room.RoomStatus,
        ) {
            roomViews[roomId] = RoomViewEntity(roomId = roomId, code = code, status = status)
        }

        override fun upsertLeader(
            roomId: Long,
            teamLeaderId: String,
            nickname: String,
            remainingBudget: Int?,
        ) {
            val current = leaders[roomId to teamLeaderId]
            leaders[roomId to teamLeaderId] =
                TeamLeaderViewEntity(
                    id = current?.id ?: seq++,
                    roomId = roomId,
                    teamLeaderId = teamLeaderId,
                    nickname = nickname,
                    remainingBudget = remainingBudget,
                )
        }
    }
}
