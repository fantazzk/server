package com.naminhyeok.fantazzk.teambuilding.model.room

import com.naminhyeok.fantazzk.teambuilding.model.TeamBuildingMode

data class Room(
    val id: RoomId,
    val code: String,
    val hostId: TeamLeaderId,
    val status: RoomStatus,
    val settings: RoomSettings,
    val playerPool: PlayerPool,
    val teamLeaders: List<TeamLeader>,
    val progression: Progression?,
    val result: RoomResult?,
) {
    fun addTeamLeader(id: TeamLeaderId, nickname: String): Room {
        check(status == RoomStatus.WAITING) { "Can only add team leaders while WAITING" }
        check(teamLeaders.size < settings.teamCount) { "Room is full" }
        check(teamLeaders.none { it.id == id }) { "Team leader already joined" }

        val leader = TeamLeader(
            id = id,
            nickname = nickname,
            remainingBudget = settings.budget,
        )
        return copy(teamLeaders = teamLeaders + leader)
    }

    fun start(): Room {
        check(status == RoomStatus.WAITING) { "Can only start from WAITING" }
        check(teamLeaders.size == settings.teamCount) { "All team leader slots must be filled" }

        val progression = when (settings.mode) {
            TeamBuildingMode.AUCTION -> Progression.Auction()
            TeamBuildingMode.DRAFT -> {
                val strategy = requireNotNull(settings.draftOrderStrategy) { "Draft requires order strategy" }
                val pickOrder = Progression.Draft.generatePickOrder(
                    teamLeaders.map { it.id },
                    strategy,
                    settings.picksPerTeam,
                )
                Progression.Draft(pickOrder = pickOrder)
            }
        }

        return copy(status = RoomStatus.IN_PROGRESS, progression = progression)
    }

    fun placeBid(teamLeaderId: TeamLeaderId, amount: Int): Room {
        check(status == RoomStatus.IN_PROGRESS) { "Room is not in progress" }
        val auction = progression as? Progression.Auction
            ?: error("Not in auction mode")

        val leader = teamLeaders.first { it.id == teamLeaderId }
        require(amount <= (leader.remainingBudget ?: 0)) { "Insufficient budget" }

        val currentHighest = auction.highestBid()?.amount ?: 0
        require(amount > currentHighest) { "Bid must be higher than current highest: $currentHighest" }

        return copy(progression = auction.addBid(Bid(teamLeaderId, amount)))
    }

    fun settleCurrentAuction(): Room {
        check(status == RoomStatus.IN_PROGRESS) { "Room is not in progress" }
        val auction = progression as? Progression.Auction
            ?: error("Not in auction mode")

        val target = requireNotNull(playerPool.currentTarget()) { "No player to auction" }
        val highestBid = auction.highestBid()

        return if (highestBid != null) {
            settleAsSold(auction, target, highestBid)
        } else {
            settleAsPassed(auction, target)
        }
    }

    private fun settleAsSold(auction: Progression.Auction, target: Player, bid: Bid): Room {
        val assignedPlayer = target.copy(status = PlayerStatus.ASSIGNED)
        val updatedLeaders = teamLeaders.map { leader ->
            if (leader.id == bid.teamLeaderId) {
                leader.deductBudget(bid.amount).addPlayer(assignedPlayer)
            } else {
                leader
            }
        }
        val updatedPool = playerPool.assignPlayer(target.name)
        val result = AuctionResult(target, AuctionResult.Outcome.Sold(bid.teamLeaderId, bid.amount))
        val updatedAuction = auction.addResult(result)

        return copy(
            teamLeaders = updatedLeaders,
            playerPool = updatedPool,
            progression = updatedAuction,
        ).checkCompletion()
    }

    private fun settleAsPassed(auction: Progression.Auction, target: Player): Room {
        val updatedPool = playerPool.moveCurrentToBack()
        val result = AuctionResult(target, AuctionResult.Outcome.Passed)
        val updatedAuction = auction.addResult(result)

        return copy(
            playerPool = updatedPool,
            progression = updatedAuction,
        )
    }

    fun pick(teamLeaderId: TeamLeaderId, playerName: String): Room {
        check(status == RoomStatus.IN_PROGRESS) { "Room is not in progress" }
        val draft = progression as? Progression.Draft
            ?: error("Not in draft mode")

        check(draft.currentTurn() == teamLeaderId) { "Not your turn" }

        val target = playerPool.players.firstOrNull { it.name == playerName && it.status == PlayerStatus.AVAILABLE }
        requireNotNull(target) { "Player '$playerName' is not available" }

        val assignedPlayer = target.copy(status = PlayerStatus.ASSIGNED)
        val updatedLeaders = teamLeaders.map { leader ->
            if (leader.id == teamLeaderId) leader.addPlayer(assignedPlayer) else leader
        }
        val updatedPool = playerPool.assignPlayer(playerName)
        val updatedDraft = draft.addPick(Pick(teamLeaderId, target)).advanceTurn()

        return copy(
            teamLeaders = updatedLeaders,
            playerPool = updatedPool,
            progression = updatedDraft,
        ).checkCompletion()
    }

    private fun checkCompletion(): Room {
        val allFull = teamLeaders.all { it.hasPickedEnough(settings.picksPerTeam) }
        if (!allFull) return this

        val updatedPool = playerPool.markRemainingAsUnassigned()
        val teams = teamLeaders.map { leader -> Team(teamLeader = leader, members = leader.team) }

        return copy(
            status = RoomStatus.COMPLETED,
            playerPool = updatedPool,
            result = RoomResult(teams),
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
            val host = TeamLeader(
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
                teamLeaders = listOf(host),
                progression = null,
                result = null,
            )
        }
    }
}
