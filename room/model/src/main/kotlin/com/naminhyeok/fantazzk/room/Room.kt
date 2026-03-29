package com.naminhyeok.fantazzk.room

import java.time.Instant

data class Room(
    override val roomId: Long = 0L,
    override val code: String,
    override val hostId: String,
    override val status: RoomStatus,
    override val mode: TeamBuildingMode,
    override val teamCount: Int,
    override val teamSize: Int,
    override val budget: Int? = null,
    override val draftOrderStrategy: DraftOrderStrategy? = null,
    override val currentTurnIndex: Int? = null,
    override val currentAuctionRound: Int? = null,
    override val createdAt: Instant = Instant.now(),
    override val updatedAt: Instant = Instant.now(),
) : RoomModel {
    init {
        when (mode) {
            TeamBuildingMode.AUCTION -> {
                requireNotNull(budget) { "경매 방에는 예산이 필요합니다" }
                require(draftOrderStrategy == null) { "경매 방에는 드래프트 순서 전략이 있으면 안 됩니다" }
            }

            TeamBuildingMode.DRAFT -> {
                require(budget == null) { "드래프트 방에는 예산이 있으면 안 됩니다" }
                requireNotNull(draftOrderStrategy) { "드래프트 방에는 순서 전략이 필요합니다" }
            }
        }
    }

    fun createHostLeader(nickname: String): RoomTeamLeader = createTeamLeader(teamLeaderId = hostId, nickname = nickname)

    fun requireJoinable(currentLeaderCount: Int): Room {
        check(isWaiting()) { "대기 중인 방에서만 참가할 수 있습니다" }
        check(currentLeaderCount < teamCount) { "방이 가득 찼습니다" }
        return this
    }

    fun join(
        teamLeaderId: String,
        nickname: String,
        currentLeaderCount: Int,
    ): RoomTeamLeader {
        requireJoinable(currentLeaderCount)
        return createTeamLeader(teamLeaderId = teamLeaderId, nickname = nickname)
    }

    fun start(leaderCount: Int): Room {
        check(isWaiting()) { "대기 중인 방에서만 시작할 수 있습니다" }
        check(leaderCount == teamCount) { "모든 팀장 자리가 채워져야 시작할 수 있습니다" }

        return when (configuration) {
            is TeamBuildingConfiguration.Auction ->
                copy(
                    status = RoomStatus.IN_PROGRESS,
                    currentAuctionRound = 1,
                    currentTurnIndex = null,
                )

            is TeamBuildingConfiguration.Draft ->
                copy(
                    status = RoomStatus.IN_PROGRESS,
                    currentTurnIndex = 0,
                    currentAuctionRound = null,
                )
        }
    }

    fun requireCurrentAuctionRound(): Int = requireNotNull(currentAuctionRound) { "현재 경매 라운드가 없습니다" }

    fun requireCurrentTurnIndex(): Int = requireNotNull(currentTurnIndex) { "현재 드래프트 턴이 없습니다" }

    fun advanceAuction(
        nextRound: Int,
        completed: Boolean,
    ): Room {
        check(isAuction()) { "경매 모드가 아닙니다" }
        check(isInProgress()) { "진행 중인 방에서만 가능합니다" }
        requireNextAuctionRound(nextRound)
        return copy(
            currentAuctionRound = nextRound,
            status = if (completed) RoomStatus.COMPLETED else status,
            currentTurnIndex = null,
        )
    }

    fun moveAuctionTargetToNextRound(nextRound: Int): Room {
        check(isAuction()) { "경매 모드가 아닙니다" }
        check(isInProgress()) { "진행 중인 방에서만 가능합니다" }
        requireNextAuctionRound(nextRound)
        return copy(
            currentAuctionRound = nextRound,
            currentTurnIndex = null,
        )
    }

    fun advanceDraftTurn(
        nextTurnIndex: Int,
        completed: Boolean,
    ): Room {
        check(isDraft()) { "드래프트 모드가 아닙니다" }
        check(isInProgress()) { "진행 중인 방에서만 가능합니다" }
        requireNextDraftTurn(nextTurnIndex)
        return copy(
            currentTurnIndex = nextTurnIndex,
            status = if (completed) RoomStatus.COMPLETED else status,
            currentAuctionRound = null,
        )
    }

    companion object {
        fun createAuction(
            code: String,
            hostId: String,
            teamCount: Int,
            teamSize: Int,
            budget: Int,
        ): Room =
            Room(
                code = code,
                hostId = hostId,
                status = RoomStatus.WAITING,
                mode = TeamBuildingMode.AUCTION,
                teamCount = teamCount,
                teamSize = teamSize,
                budget = budget,
            )

        fun createDraft(
            code: String,
            hostId: String,
            teamCount: Int,
            teamSize: Int,
            draftOrderStrategy: DraftOrderStrategy,
        ): Room =
            Room(
                code = code,
                hostId = hostId,
                status = RoomStatus.WAITING,
                mode = TeamBuildingMode.DRAFT,
                teamCount = teamCount,
                teamSize = teamSize,
                draftOrderStrategy = draftOrderStrategy,
            )

        fun from(model: RoomModel): Room =
            Room(
                roomId = model.roomId,
                code = model.code,
                hostId = model.hostId,
                status = model.status,
                mode = model.mode,
                teamCount = model.teamCount,
                teamSize = model.teamSize,
                budget = if (model.mode == TeamBuildingMode.AUCTION) model.budget else null,
                draftOrderStrategy = if (model.mode == TeamBuildingMode.DRAFT) model.draftOrderStrategy else null,
                currentTurnIndex = model.currentTurnIndex,
                currentAuctionRound = model.currentAuctionRound,
                createdAt = model.createdAt,
                updatedAt = model.updatedAt,
            )
    }

    private fun createTeamLeader(
        teamLeaderId: String,
        nickname: String,
    ): RoomTeamLeader =
        RoomTeamLeader(
            roomId = roomId,
            teamLeaderId = teamLeaderId,
            nickname = nickname,
            remainingBudget =
                when (val configuration = configuration) {
                    is TeamBuildingConfiguration.Auction -> configuration.budget
                    is TeamBuildingConfiguration.Draft -> null
                },
        )

    private fun requireNextAuctionRound(nextRound: Int) {
        val currentRound = requireCurrentAuctionRound()
        require(nextRound > currentRound) { "다음 경매 라운드는 현재보다 커야 합니다" }
    }

    private fun requireNextDraftTurn(nextTurnIndex: Int) {
        val currentIndex = requireCurrentTurnIndex()
        require(nextTurnIndex > currentIndex) { "다음 드래프트 턴은 현재보다 커야 합니다" }
    }
}

fun RoomModel.requireCurrentAuctionRound(): Int = Room.from(this).requireCurrentAuctionRound()

fun RoomModel.requireCurrentTurnIndex(): Int = Room.from(this).requireCurrentTurnIndex()

fun RoomModel.advanceAuction(
    nextRound: Int,
    completed: Boolean,
): Room = Room.from(this).advanceAuction(nextRound = nextRound, completed = completed)

fun RoomModel.moveAuctionTargetToNextRound(nextRound: Int): Room = Room.from(this).moveAuctionTargetToNextRound(nextRound = nextRound)

fun RoomModel.advanceDraftTurn(
    nextTurnIndex: Int,
    completed: Boolean,
): Room = Room.from(this).advanceDraftTurn(nextTurnIndex = nextTurnIndex, completed = completed)
