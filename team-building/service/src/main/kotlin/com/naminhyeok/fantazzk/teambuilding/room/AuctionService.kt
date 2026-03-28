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
        val room = findInProgressRoom(code)
        check(room.isAuction()) { "경매 모드가 아닙니다" }

        val leader =
            roomTeamLeaderRepository.findByRoomIdAndTeamLeaderId(room.roomId, teamLeaderId)
                ?: throw RoomTeamLeaderNotFoundException()
        (leader as RoomTeamLeader).validateBudget(amount)

        val currentRound = roomTeamMemberRepository.countByRoomId(room.roomId) + 1
        val highest = roomBidRepository.findHighestByRoomIdAndRound(room.roomId, currentRound)
        require(amount > (highest?.amount ?: 0)) { "현재 최고가보다 높아야 합니다" }

        return roomBidRepository.save(
            RoomBid(roomId = room.roomId, round = currentRound, teamLeaderId = teamLeaderId, amount = amount),
        )
    }

    override fun settle(code: String): AuctionSettleResult {
        val room = findInProgressRoom(code)
        check(room.isAuction()) { "경매 모드가 아닙니다" }

        val target = requireNotNull(roomPlayerRepository.findFirstAvailable(room.roomId)) { "경매할 선수가 없습니다" }
        val currentRound = roomTeamMemberRepository.countByRoomId(room.roomId) + 1
        val highest = roomBidRepository.findHighestByRoomIdAndRound(room.roomId, currentRound)

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
        roomPlayerRepository.updateStatus(target.roomPlayerId, PlayerStatus.ASSIGNED)

        val winner =
            roomTeamLeaderRepository.findByRoomIdAndTeamLeaderId(room.roomId, bid.teamLeaderId)
                ?: throw RoomTeamLeaderNotFoundException()
        roomTeamLeaderRepository.updateRemainingBudget(winner.roomTeamLeaderId, winner.remainingBudget!! - bid.amount)

        val assignOrder = roomTeamMemberRepository.countByRoomId(room.roomId)
        roomTeamMemberRepository.save(
            RoomTeamMember(
                roomId = room.roomId,
                teamLeaderId = bid.teamLeaderId,
                playerName = target.name,
                assignOrder = assignOrder,
            ),
        )

        checkCompletion(room)
        return AuctionSettleResult(target.name, AuctionOutcome.SOLD)
    }

    private fun settlePassed(
        room: RoomModel,
        target: RoomPlayerModel,
    ): AuctionSettleResult {
        roomPlayerRepository.moveToBack(room.roomId, target.roomPlayerId)
        return AuctionSettleResult(target.name, AuctionOutcome.PASSED)
    }

    private fun checkCompletion(room: RoomModel) {
        val totalRequired = room.teamCount * (room.teamSize - 1)
        val assigned = roomTeamMemberRepository.countByRoomId(room.roomId)
        if (assigned >= totalRequired) {
            roomRepository.updateStatus(room.roomId, RoomStatus.COMPLETED)
        }
    }

    private fun findInProgressRoom(code: String): RoomModel {
        val room = roomRepository.findByCode(code) ?: throw RoomNotFoundException()
        check(room.isInProgress()) { "진행 중인 방에서만 가능합니다" }
        return room
    }
}
