package com.naminhyeok.fantazzk.teambuilding.room

import com.naminhyeok.fantazzk.teambuilding.TeamBuildingMode

data class Room(
    val id: RoomId,
    val code: String,
    val hostId: TeamLeaderId,
    val status: RoomStatus,
    val settings: RoomSettings,
    val playerPool: PlayerPool,
    val teamLeaders: TeamLeaders,
    val progression: Progression?,
    val result: RoomResult?,
) {
    fun addTeamLeader(
        id: TeamLeaderId,
        nickname: String,
    ): Room {
        check(status == RoomStatus.WAITING) { "대기 중인 방에서만 팀장을 추가할 수 있습니다" }
        check(!teamLeaders.isFull(settings.teamCount)) { "방이 가득 찼습니다" }
        check(!teamLeaders.contains(id)) { "이미 참가한 팀장입니다" }

        val leader =
            TeamLeader(
                id = id,
                nickname = nickname,
                remainingBudget = settings.budget,
            )
        return copy(teamLeaders = teamLeaders.add(leader))
    }

    fun start(): Room {
        check(status == RoomStatus.WAITING) { "대기 중인 방에서만 시작할 수 있습니다" }
        check(teamLeaders.size == settings.teamCount) { "모든 팀장 자리가 채워져야 시작할 수 있습니다" }

        val progression =
            when (settings.mode) {
                TeamBuildingMode.AUCTION -> Progression.Auction()
                TeamBuildingMode.DRAFT -> {
                    val strategy = requireNotNull(settings.draftOrderStrategy) { "드래프트에는 픽 순서 전략이 필요합니다" }
                    val pickOrder =
                        Progression.Draft.generatePickOrder(
                            teamLeaders.ids(),
                            strategy,
                            settings.picksPerTeam,
                        )
                    Progression.Draft(pickOrder = pickOrder)
                }
            }

        return copy(status = RoomStatus.IN_PROGRESS, progression = progression)
    }

    fun placeBid(
        teamLeaderId: TeamLeaderId,
        amount: Int,
    ): Room {
        check(status == RoomStatus.IN_PROGRESS) { "진행 중인 방에서만 가능합니다" }
        val auction =
            progression as? Progression.Auction
                ?: error("경매 모드가 아닙니다")

        val leader = teamLeaders.findById(teamLeaderId)
        require(amount <= (leader.remainingBudget ?: 0)) { "예산이 부족합니다" }

        val currentHighest = auction.highestBid()?.amount ?: 0
        require(amount > currentHighest) { "현재 최고가($currentHighest)보다 높아야 합니다" }

        return copy(progression = auction.addBid(Bid(teamLeaderId, amount)))
    }

    fun settleCurrentAuction(): Room {
        check(status == RoomStatus.IN_PROGRESS) { "진행 중인 방에서만 가능합니다" }
        val auction =
            progression as? Progression.Auction
                ?: error("경매 모드가 아닙니다")

        val target = requireNotNull(playerPool.currentTarget()) { "경매할 선수가 없습니다" }
        val highestBid = auction.highestBid()

        return if (highestBid != null) {
            settleAsSold(auction, target, highestBid)
        } else {
            settleAsPassed(auction, target)
        }
    }

    private fun settleAsSold(
        auction: Progression.Auction,
        target: Player,
        bid: Bid,
    ): Room {
        val assignedPlayer = target.copy(status = PlayerStatus.ASSIGNED)
        val updatedLeaders =
            teamLeaders.update(bid.teamLeaderId) { it.deductBudget(bid.amount).addPlayer(assignedPlayer) }
        val updatedPool = playerPool.assignPlayer(target.name)
        val result = AuctionResult(target, AuctionResult.Outcome.Sold(bid.teamLeaderId, bid.amount))
        val updatedAuction = auction.addResult(result)

        return copy(
            teamLeaders = updatedLeaders,
            playerPool = updatedPool,
            progression = updatedAuction,
        ).checkCompletion()
    }

    private fun settleAsPassed(
        auction: Progression.Auction,
        target: Player,
    ): Room {
        val updatedPool = playerPool.moveCurrentToBack()
        val result = AuctionResult(target, AuctionResult.Outcome.Passed)
        val updatedAuction = auction.addResult(result)

        return copy(
            playerPool = updatedPool,
            progression = updatedAuction,
        )
    }

    fun pick(
        teamLeaderId: TeamLeaderId,
        playerName: String,
    ): Room {
        check(status == RoomStatus.IN_PROGRESS) { "진행 중인 방에서만 가능합니다" }
        val draft =
            progression as? Progression.Draft
                ?: error("드래프트 모드가 아닙니다")

        check(!draft.isFinished()) { "드래프트가 이미 종료되었습니다" }
        check(draft.currentTurn() == teamLeaderId) { "현재 턴이 아닙니다" }

        val target = playerPool.players.firstOrNull { it.name == playerName && it.status == PlayerStatus.AVAILABLE }
        requireNotNull(target) { "선수 '$playerName'은(는) 선택할 수 없습니다" }

        val assignedPlayer = target.copy(status = PlayerStatus.ASSIGNED)
        val updatedLeaders = teamLeaders.update(teamLeaderId) { it.addPlayer(assignedPlayer) }
        val updatedPool = playerPool.assignPlayer(playerName)
        val updatedDraft = draft.addPick(Pick(teamLeaderId, playerName)).advanceTurn()

        return copy(
            teamLeaders = updatedLeaders,
            playerPool = updatedPool,
            progression = updatedDraft,
        ).checkCompletion()
    }

    private fun checkCompletion(): Room {
        if (!teamLeaders.allPickedEnough(settings.picksPerTeam)) return this

        val updatedPool = playerPool.markRemainingAsUnassigned()

        return copy(
            status = RoomStatus.COMPLETED,
            playerPool = updatedPool,
            result = RoomResult(teamLeaders.toTeams()),
        )
    }

    companion object {
        fun create(
            id: RoomId,
            code: String,
            hostId: TeamLeaderId,
            hostNickname: String,
            settings: RoomSettings,
            playerPool: PlayerPool,
        ): Room {
            val host =
                TeamLeader(
                    id = hostId,
                    nickname = hostNickname,
                    remainingBudget = settings.budget,
                )
            return Room(
                id = id,
                code = code,
                hostId = hostId,
                status = RoomStatus.WAITING,
                settings = settings,
                playerPool = playerPool,
                teamLeaders = TeamLeaders(listOf(host)),
                progression = null,
                result = null,
            )
        }
    }
}
