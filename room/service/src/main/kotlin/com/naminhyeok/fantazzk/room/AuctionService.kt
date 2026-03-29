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
        AuctionRound(round = currentRound, highestBid = highest).requireHigherBid(amount)

        return roomBidRepository.save(
            RoomBid(roomId = room.roomId, round = currentRound, teamLeaderId = teamLeaderId, amount = amount),
        )
    }

    override fun settle(code: String): AuctionSettleResult {
        val room = findInProgressAuctionRoom(code)
        val currentRound = room.requireCurrentAuctionRound()

        val target = requireNotNull(roomPlayerRepository.findFirstAvailable(room.roomId)) { "경매할 선수가 없습니다" }
        val highest = roomBidRepository.findHighestByRoomIdAndRound(room.roomId, currentRound)
        val assignedCountAfterSettlement = roomTeamMemberRepository.countByRoomId(room.roomId) + 1
        val totalRequired = room.teamCount * room.picksPerTeam
        val settlement =
            AuctionRound(round = currentRound, highestBid = highest).settle(
                playerName = target.name,
                assignedCountAfterSettlement = assignedCountAfterSettlement,
                totalRequired = totalRequired,
            )

        return if (settlement.outcome == AuctionOutcome.SOLD) {
            settleSold(room, target, settlement)
        } else {
            settlePassed(room, target, settlement)
        }
    }

    private fun settleSold(
        room: RoomModel,
        target: RoomPlayerModel,
        settlement: AuctionRoundSettlement,
    ): AuctionSettleResult {
        val winningBid = requireNotNull(settlement.winningBid) { "낙찰 정산에는 최고 입찰이 필요합니다" }
        val winner =
            roomTeamLeaderRepository.findByRoomIdAndTeamLeaderId(room.roomId, winningBid.teamLeaderId)
                ?: throw RoomException.TeamLeaderNotFoundException()

        val leaderMemberCount =
            roomTeamMemberRepository.countByRoomIdAndTeamLeaderId(room.roomId, winningBid.teamLeaderId)
        AuctionRound(round = winningBid.round, highestBid = winningBid).requireRosterCapacity(
            currentMemberCount = leaderMemberCount,
            picksPerTeam = room.picksPerTeam,
        )

        val assignedCount = roomTeamMemberRepository.countByRoomId(room.roomId)
        val nextRoom = room.advanceAuction(nextRound = settlement.nextRound, completed = settlement.completed)
        val assignedPlayer = target.assign()
        val updatedWinner = winner.spend(winningBid.amount)
        val member =
            RoomTeamMember(
                roomId = room.roomId,
                teamLeaderId = winningBid.teamLeaderId,
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
        settlement: AuctionRoundSettlement,
    ): AuctionSettleResult {
        val nextRoom = room.moveAuctionTargetToNextRound(nextRound = settlement.nextRound)
        val players = roomPlayerRepository.findByRoomId(room.roomId)
        val maxOrder = players.maxOf { it.displayOrder }
        val movedTarget = target.moveToBack(maxOrder + 1)

        roomPlayerRepository.save(movedTarget)
        roomRepository.save(nextRoom)
        return AuctionSettleResult(settlement.playerName, settlement.outcome)
    }

    private fun findInProgressAuctionRoom(code: String): RoomModel {
        val room = roomRepository.findByCode(code) ?: throw RoomException.RoomNotFoundException()
        check(room.isInProgress()) { "진행 중인 방에서만 가능합니다" }
        check(room.isAuction()) { "경매 모드가 아닙니다" }
        return room
    }
}
