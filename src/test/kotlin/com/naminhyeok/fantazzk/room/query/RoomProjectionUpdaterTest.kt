package com.naminhyeok.fantazzk.room.query

import com.naminhyeok.fantazzk.room.AuctionOutcome
import com.naminhyeok.fantazzk.room.AuctionSettled
import com.naminhyeok.fantazzk.room.LeaderSnapshot
import com.naminhyeok.fantazzk.room.RoomCompleted
import com.naminhyeok.fantazzk.room.RoomCreated
import com.naminhyeok.fantazzk.room.RoomJoined
import com.naminhyeok.fantazzk.room.RoomStarted
import com.naminhyeok.fantazzk.room.RoomStatus
import io.mockk.mockk
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class RoomProjectionUpdaterTest {
    private lateinit var writer: InMemoryRoomProjectionWriter
    private lateinit var cut: RoomProjectionUpdater

    @BeforeEach
    fun setUp() {
        writer = InMemoryRoomProjectionWriter()
        cut = RoomProjectionUpdater(writer)
    }

    @Test
    fun `RoomCreated 이벤트로 room view 와 host leader projection 을 만든다`() {
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
    fun `RoomJoined 이벤트로 leader projection 을 추가한다`() {
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

    @Test
    fun `RoomStarted 이벤트로 room status projection 을 갱신한다`() {
        cut.on(
            RoomStarted(
                roomId = 1L,
                code = "ROOM01",
                status = RoomStatus.IN_PROGRESS,
                mode = RoomStarted.Mode.AUCTION,
            ),
        )

        assertThat(writer.roomViews[1L]?.status).isEqualTo(RoomStatus.IN_PROGRESS)
    }

    @Test
    fun `RoomCompleted 이벤트로 room status projection 을 완료 상태로 갱신한다`() {
        cut.on(
            RoomCompleted(
                roomId = 1L,
                code = "ROOM01",
                status = RoomStatus.COMPLETED,
                mode = RoomStarted.Mode.AUCTION,
            ),
        )

        assertThat(writer.roomViews[1L]?.status).isEqualTo(RoomStatus.COMPLETED)
    }

    @Test
    fun `AuctionSettled 이벤트로 leader budget projection 을 갱신한다`() {
        cut.on(
            AuctionSettled(
                roomId = 1L,
                code = "ROOM01",
                playerName = "선수1",
                outcome = AuctionOutcome.SOLD,
                leaders =
                    listOf(
                        LeaderSnapshot(teamLeaderId = "leader-A", nickname = "팀장A", remainingBudget = 300),
                        LeaderSnapshot(teamLeaderId = "leader-B", nickname = "팀장B", remainingBudget = 150),
                    ),
            ),
        )

        assertThat(writer.leaders[1L to "leader-A"]?.remainingBudget).isEqualTo(300)
        assertThat(writer.leaders[1L to "leader-B"]?.remainingBudget).isEqualTo(150)
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
