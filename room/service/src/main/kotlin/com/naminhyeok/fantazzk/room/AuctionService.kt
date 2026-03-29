package com.naminhyeok.fantazzk.room

import com.naminhyeok.fantazzk.room.exception.RoomException
import com.naminhyeok.fantazzk.room.repository.RoomBidRepository
import com.naminhyeok.fantazzk.room.repository.RoomPlayerRepository
import com.naminhyeok.fantazzk.room.repository.RoomRepository
import com.naminhyeok.fantazzk.room.repository.RoomTeamLeaderRepository
import com.naminhyeok.fantazzk.room.repository.RoomTeamMemberRepository

data class AuctionSettleResult(
    val playerName: String,
    val outcome: AuctionOutcome,
)

interface AuctionService {
    fun placeBid(
        code: String,
        teamLeaderId: String,
        amount: Int,
    ): RoomBidModel

    fun settle(code: String): AuctionSettleResult
}

internal class AuctionServiceImpl(
    private val roomRepository: RoomRepository,
    private val roomTeamLeaderRepository: RoomTeamLeaderRepository,
    private val roomPlayerRepository: RoomPlayerRepository,
    private val roomTeamMemberRepository: RoomTeamMemberRepository,
    private val roomBidRepository: RoomBidRepository,
) : AuctionService {
    override fun placeBid(
        code: String,
        teamLeaderId: String,
        amount: Int,
    ): RoomBidModel {
        val room = findInProgressAuctionRoom(code)
        val currentRound = room.requireCurrentAuctionRound()

        val leader =
            roomTeamLeaderRepository.findByRoomIdAndTeamLeaderId(room.roomId, teamLeaderId)
                ?: throw RoomException.TeamLeaderNotFoundException()

        leader.requireCanBid(amount)

        val highest = roomBidRepository.findHighestByRoomIdAndRound(room.roomId, currentRound)
        require(amount > (highest?.amount ?: 0)) { "현재 최고가보다 높아야 합니다" }

        return roomBidRepository.save(
            RoomBid(roomId = room.roomId, round = currentRound, teamLeaderId = teamLeaderId, amount = amount),
        )
    }

    override fun settle(code: String): AuctionSettleResult {
        val room = findInProgressAuctionRoom(code)
        val currentRound = room.requireCurrentAuctionRound()

        val target = requireNotNull(roomPlayerRepository.findFirstAvailable(room.roomId)) { "경매할 선수가 없습니다" }
        val nextRound = currentRound + 1
        val highest = roomBidRepository.findHighestByRoomIdAndRound(room.roomId, currentRound)

        return if (highest != null) {
            settleSold(room, target, highest, nextRound)
        } else {
            settlePassed(room, target, nextRound)
        }
    }

    private fun settleSold(
        room: RoomModel,
        target: RoomPlayerModel,
        bid: RoomBidModel,
        nextRound: Int,
    ): AuctionSettleResult {
        val winner =
            roomTeamLeaderRepository.findByRoomIdAndTeamLeaderId(room.roomId, bid.teamLeaderId)
                ?: throw RoomException.TeamLeaderNotFoundException()

        val leaderMemberCount =
            roomTeamMemberRepository.countByRoomIdAndTeamLeaderId(room.roomId, bid.teamLeaderId)
        check(leaderMemberCount < room.picksPerTeam) { "팀장의 팀원 정원이 가득 찼습니다" }

        val assignedCount = roomTeamMemberRepository.countByRoomId(room.roomId)
        val totalRequired = room.teamCount * room.picksPerTeam
        val completed = assignedCount + 1 >= totalRequired
        val nextRoom = room.advanceAuction(nextRound = nextRound, completed = completed)
        val assignedPlayer = target.assign()
        val updatedWinner = winner.spend(bid.amount)
        val member =
            RoomTeamMember(
                roomId = room.roomId,
                teamLeaderId = bid.teamLeaderId,
                playerName = target.name,
                assignOrder = assignedCount,
            )

        roomPlayerRepository.save(assignedPlayer)
        roomTeamLeaderRepository.save(updatedWinner)
        roomTeamMemberRepository.save(member)
        roomRepository.save(nextRoom)

        return AuctionSettleResult(target.name, AuctionOutcome.SOLD)
    }

    private fun settlePassed(
        room: RoomModel,
        target: RoomPlayerModel,
        nextRound: Int,
    ): AuctionSettleResult {
        val nextRoom = room.moveAuctionTargetToNextRound(nextRound = nextRound)
        val players = roomPlayerRepository.findByRoomId(room.roomId)
        val maxOrder = players.maxOf { it.displayOrder }
        val movedTarget = target.moveToBack(maxOrder + 1)

        roomPlayerRepository.save(movedTarget)
        roomRepository.save(nextRoom)
        return AuctionSettleResult(target.name, AuctionOutcome.PASSED)
    }

    private fun findInProgressAuctionRoom(code: String): RoomModel {
        val room = roomRepository.findByCode(code) ?: throw RoomException.RoomNotFoundException()
        check(room.isInProgress()) { "진행 중인 방에서만 가능합니다" }
        check(room.isAuction()) { "경매 모드가 아닙니다" }
        return room
    }
}
