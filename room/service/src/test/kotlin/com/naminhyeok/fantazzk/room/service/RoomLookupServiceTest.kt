package com.naminhyeok.fantazzk.room.service

import com.naminhyeok.fantazzk.room.exception.RoomException
import com.naminhyeok.fantazzk.room.model.Room
import com.naminhyeok.fantazzk.room.model.RoomBid
import com.naminhyeok.fantazzk.room.model.RoomPlayer
import com.naminhyeok.fantazzk.room.model.RoomStatus
import com.naminhyeok.fantazzk.room.model.RoomTeamLeader
import com.naminhyeok.fantazzk.room.model.RoomTeamMember
import com.naminhyeok.fantazzk.room.model.TeamBuildingMode
import com.naminhyeok.fantazzk.room.service.support.InMemoryRoomBidRepository
import com.naminhyeok.fantazzk.room.service.support.InMemoryRoomPlayerRepository
import com.naminhyeok.fantazzk.room.service.support.InMemoryRoomRepository
import com.naminhyeok.fantazzk.room.service.support.InMemoryRoomTeamLeaderRepository
import com.naminhyeok.fantazzk.room.service.support.InMemoryRoomTeamMemberRepository
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class RoomLookupServiceTest {
    private lateinit var roomRepo: InMemoryRoomRepository
    private lateinit var playerRepo: InMemoryRoomPlayerRepository
    private lateinit var leaderRepo: InMemoryRoomTeamLeaderRepository
    private lateinit var memberRepo: InMemoryRoomTeamMemberRepository
    private lateinit var bidRepo: InMemoryRoomBidRepository
    private lateinit var cut: RoomLookupService

    private var roomId: Long = 0L

    @BeforeEach
    fun setUp() {
        roomRepo = InMemoryRoomRepository()
        playerRepo = InMemoryRoomPlayerRepository()
        leaderRepo = InMemoryRoomTeamLeaderRepository()
        memberRepo = InMemoryRoomTeamMemberRepository()
        bidRepo = InMemoryRoomBidRepository()
        cut = RoomLookupServiceImpl(roomRepo, playerRepo, leaderRepo, memberRepo, bidRepo)

        val room =
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
            )
        roomId = room.roomId
    }

    @Test
    fun `코드로 방을 조회할 수 있다`() {
        val room = cut.get("LOOK01")
        assertThat(room.code).isEqualTo("LOOK01")
    }

    @Test
    fun `존재하지 않는 코드로 조회하면 예외가 발생한다`() {
        assertThatThrownBy { cut.get("NOCODE") }
            .isInstanceOf(RoomException.RoomNotFoundException::class.java)
    }

    @Test
    fun `방의 선수 목록을 조회할 수 있다`() {
        playerRepo.saveAll(
            listOf(
                RoomPlayer(roomId = roomId, name = "선수1", displayOrder = 0),
                RoomPlayer(roomId = roomId, name = "선수2", displayOrder = 1),
            ),
        )

        val players = cut.getPlayers(roomId)
        assertThat(players).hasSize(2)
    }

    @Test
    fun `방의 팀장 목록을 조회할 수 있다`() {
        leaderRepo.save(RoomTeamLeader(roomId = roomId, teamLeaderId = "leader-A", nickname = "팀장A"))

        val leaders = cut.getTeamLeaders(roomId)
        assertThat(leaders).hasSize(1)
        assertThat(leaders.first().nickname).isEqualTo("팀장A")
    }

    @Test
    fun `방의 팀원 목록을 조회할 수 있다`() {
        memberRepo.save(RoomTeamMember(roomId = roomId, teamLeaderId = "leader-A", playerName = "선수1", assignOrder = 0))

        val members = cut.getTeamMembers(roomId)
        assertThat(members).hasSize(1)
        assertThat(members.first().playerName).isEqualTo("선수1")
    }

    @Test
    fun `방의 입찰 기록을 라운드별로 조회할 수 있다`() {
        bidRepo.save(RoomBid(roomId = roomId, round = 1, teamLeaderId = "leader-A", amount = 100))
        bidRepo.save(RoomBid(roomId = roomId, round = 1, teamLeaderId = "leader-B", amount = 150))
        bidRepo.save(RoomBid(roomId = roomId, round = 2, teamLeaderId = "leader-A", amount = 200))

        val round1Bids = cut.getBids(roomId, 1)
        val round2Bids = cut.getBids(roomId, 2)

        assertThat(round1Bids).hasSize(2)
        assertThat(round2Bids).hasSize(1)
    }
}
