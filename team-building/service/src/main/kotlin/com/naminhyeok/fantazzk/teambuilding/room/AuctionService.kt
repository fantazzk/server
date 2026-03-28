package com.naminhyeok.fantazzk.teambuilding.room

import com.naminhyeok.fantazzk.teambuilding.exception.RoomNotFoundException
import com.naminhyeok.fantazzk.teambuilding.exception.RoomTeamLeaderNotFoundException
import com.naminhyeok.fantazzk.teambuilding.room.repository.RoomBidRepository
import com.naminhyeok.fantazzk.teambuilding.room.repository.RoomPlayerRepository
import com.naminhyeok.fantazzk.teambuilding.room.repository.RoomRepository
import com.naminhyeok.fantazzk.teambuilding.room.repository.RoomTeamLeaderRepository
import com.naminhyeok.fantazzk.teambuilding.room.repository.RoomTeamMemberRepository

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

        val leader =
            roomTeamLeaderRepository.findByRoomIdAndTeamLeaderId(room.roomId, teamLeaderId)
                ?: throw RoomTeamLeaderNotFoundException()

        val budget = requireNotNull(leader.remainingBudget) { "이 모드에서는 예산이 존재하지 않습니다" }
        require(amount <= budget) { "예산이 부족합니다: 잔여 $budget, 필요 $amount" }

        val currentRound = room.currentAuctionRound ?: 1
        val highest = roomBidRepository.findHighestByRoomIdAndRound(room.roomId, currentRound)
        require(amount > (highest?.amount ?: 0)) { "현재 최고가보다 높아야 합니다" }

        return roomBidRepository.save(
            RoomBid(roomId = room.roomId, round = currentRound, teamLeaderId = teamLeaderId, amount = amount),
        )
    }

    override fun settle(code: String): AuctionSettleResult {
        val room = findInProgressAuctionRoom(code)

        val target = requireNotNull(roomPlayerRepository.findFirstAvailable(room.roomId)) { "경매할 선수가 없습니다" }
        val currentRound = room.currentAuctionRound ?: 1
        val highest = roomBidRepository.findHighestByRoomIdAndRound(room.roomId, currentRound)

        roomRepository.save(Room.from(room).copy(currentAuctionRound = currentRound + 1))

        return if (highest != null) {
            settleSold(room, target, highest)
        } else {
            settlePassed(room, target)
        }
    }

    private fun settleSold(
        room: RoomModel,
        target: RoomPlayerModel,
        bid: RoomBidModel,
    ): AuctionSettleResult {
        roomPlayerRepository.save(RoomPlayer.from(target).copy(status = PlayerStatus.ASSIGNED))

        val winner =
            roomTeamLeaderRepository.findByRoomIdAndTeamLeaderId(room.roomId, bid.teamLeaderId)
                ?: throw RoomTeamLeaderNotFoundException()
        roomTeamLeaderRepository.save(
            RoomTeamLeader.from(winner).copy(remainingBudget = winner.remainingBudget!! - bid.amount),
        )

        val assignedCount = roomTeamMemberRepository.countByRoomId(room.roomId)
        roomTeamMemberRepository.save(
            RoomTeamMember(
                roomId = room.roomId,
                teamLeaderId = bid.teamLeaderId,
                playerName = target.name,
                assignOrder = assignedCount,
            ),
        )

        val totalRequired = room.teamCount * room.picksPerTeam
        if (assignedCount + 1 >= totalRequired) {
            roomRepository.save(
                Room.from(room).copy(status = RoomStatus.COMPLETED, currentAuctionRound = (room.currentAuctionRound ?: 1) + 1),
            )
        }

        return AuctionSettleResult(target.name, AuctionOutcome.SOLD)
    }

    private fun settlePassed(
        room: RoomModel,
        target: RoomPlayerModel,
    ): AuctionSettleResult {
        val players = roomPlayerRepository.findByRoomId(room.roomId)
        val maxOrder = players.maxOf { it.displayOrder }
        roomPlayerRepository.save(RoomPlayer.from(target).copy(displayOrder = maxOrder + 1))
        return AuctionSettleResult(target.name, AuctionOutcome.PASSED)
    }

    private fun findInProgressAuctionRoom(code: String): RoomModel {
        val room = roomRepository.findByCode(code) ?: throw RoomNotFoundException()
        check(room.isInProgress()) { "진행 중인 방에서만 가능합니다" }
        check(room.isAuction()) { "경매 모드가 아닙니다" }
        return room
    }
}
