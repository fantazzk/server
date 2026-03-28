package com.naminhyeok.fantazzk.teambuilding.room

import com.naminhyeok.fantazzk.teambuilding.TeamBuildingMode
import com.naminhyeok.fantazzk.teambuilding.support.InMemoryRoomBidRepository
import com.naminhyeok.fantazzk.teambuilding.support.InMemoryRoomPlayerRepository
import com.naminhyeok.fantazzk.teambuilding.support.InMemoryRoomRepository
import com.naminhyeok.fantazzk.teambuilding.support.InMemoryRoomTeamLeaderRepository
import com.naminhyeok.fantazzk.teambuilding.support.InMemoryRoomTeamMemberRepository
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class AuctionServiceTest {
    private lateinit var roomRepo: InMemoryRoomRepository
    private lateinit var playerRepo: InMemoryRoomPlayerRepository
    private lateinit var leaderRepo: InMemoryRoomTeamLeaderRepository
    private lateinit var memberRepo: InMemoryRoomTeamMemberRepository
    private lateinit var bidRepo: InMemoryRoomBidRepository
    private lateinit var auctionService: AuctionService

    private lateinit var roomCode: String
    private var roomId: Long = 0L

    @BeforeEach
    fun setUp() {
        roomRepo = InMemoryRoomRepository()
        playerRepo = InMemoryRoomPlayerRepository()
        leaderRepo = InMemoryRoomTeamLeaderRepository()
        memberRepo = InMemoryRoomTeamMemberRepository()
        bidRepo = InMemoryRoomBidRepository()
        auctionService = AuctionServiceImpl(roomRepo, leaderRepo, playerRepo, memberRepo, bidRepo)

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

    @Test
    fun `입찰하면 입찰 기록이 저장된다`() {
        val bid = auctionService.placeBid(roomCode, "leader-A", 100)

        assertThat(bid.amount).isEqualTo(100)
        assertThat(bid.teamLeaderId).isEqualTo("leader-A")
    }

    @Test
    fun `예산을 초과하여 입찰할 수 없다`() {
        assertThatThrownBy { auctionService.placeBid(roomCode, "leader-A", 301) }
            .isInstanceOf(IllegalArgumentException::class.java)
    }

    @Test
    fun `낙찰 시 선수가 팀에 배정되고 예산이 차감된다`() {
        auctionService.placeBid(roomCode, "leader-A", 100)
        auctionService.placeBid(roomCode, "leader-B", 150)
        val result = auctionService.settle(roomCode)

        assertThat(result.playerName).isEqualTo("선수1")
        assertThat(result.outcome).isEqualTo(AuctionOutcome.SOLD)

        val members = memberRepo.findByRoomIdAndTeamLeaderId(roomId, "leader-B")
        assertThat(members).hasSize(1)
        assertThat(members.first().playerName).isEqualTo("선수1")
    }

    @Test
    fun `유찰 시 선수가 풀 뒤로 이동한다`() {
        val result = auctionService.settle(roomCode)

        assertThat(result.outcome).isEqualTo(AuctionOutcome.PASSED)
        assertThat(result.playerName).isEqualTo("선수1")

        val nextTarget = playerRepo.findFirstAvailable(roomId)
        assertThat(nextTarget?.name).isEqualTo("선수2")
    }

    @Test
    fun `유찰 후 다음 경매에서 입찰이 이전 라운드와 혼동되지 않는다`() {
        // 라운드 1: 선수1 유찰
        auctionService.settle(roomCode)

        // 라운드 2: 선수2에 대해 입찰
        auctionService.placeBid(roomCode, "leader-A", 100)
        val result = auctionService.settle(roomCode)

        // 선수2가 낙찰되어야 함 (선수1이 아님)
        assertThat(result.playerName).isEqualTo("선수2")
        assertThat(result.outcome).isEqualTo(AuctionOutcome.SOLD)
    }

    @Test
    fun `유찰 후 새 입찰의 라운드 번호가 이전 유찰 라운드와 달라야 한다`() {
        // 라운드 1: 선수1에 대해 leader-A가 50에 입찰
        auctionService.placeBid(roomCode, "leader-A", 50)
        // 라운드 1: 유찰 (settle 시 입찰이 있으므로 낙찰됨 — 이 테스트는 다른 시나리오)
        // 대신: 입찰 없이 유찰
        // 리셋: 새로운 setup 필요

        // 시나리오: 라운드1에서 50 입찰 → 낙찰, 라운드2에서 유찰, 라운드3에서 입찰
        auctionService.settle(roomCode) // 라운드 1: 선수1 → leader-A (50에 낙찰)

        auctionService.settle(roomCode) // 라운드 2: 선수2 유찰 (입찰 없음)

        // 라운드 3: 선수3에 대해 입찰 — 이 입찰의 round가 2가 아닌 3이어야 함
        auctionService.placeBid(roomCode, "leader-B", 80)
        val settleResult = auctionService.settle(roomCode)

        assertThat(settleResult.playerName).isEqualTo("선수3")
        assertThat(settleResult.outcome).isEqualTo(AuctionOutcome.SOLD)

        // leader-B의 팀에 선수3이 있어야 함
        val bMembers = memberRepo.findByRoomIdAndTeamLeaderId(roomId, "leader-B")
        assertThat(bMembers).hasSize(1)
        assertThat(bMembers.first().playerName).isEqualTo("선수3")
    }

    @Test
    fun `모든 팀 정원이 채워지면 방이 완료된다`() {
        // 선수1 -> leader-A
        auctionService.placeBid(roomCode, "leader-A", 100)
        auctionService.settle(roomCode)

        // 선수2 -> leader-B
        auctionService.placeBid(roomCode, "leader-B", 100)
        auctionService.settle(roomCode)

        val room = roomRepo.findByCode(roomCode)!!
        assertThat(room.status).isEqualTo(RoomStatus.COMPLETED)
    }
}
