package com.naminhyeok.fantazzk.room

import com.naminhyeok.fantazzk.room.exception.RoomException
import com.naminhyeok.fantazzk.template.TemplateBlueprint
import com.naminhyeok.fantazzk.template.TemplateMode
import org.jmolecules.ddd.types.AggregateRoot
import org.springframework.data.annotation.Transient
import java.time.Instant
import java.util.UUID

data class Room(
    val roomId: Long = 0L,
    val code: String,
    val hostId: String,
    val status: RoomStatus,
    val mode: TeamBuildingMode,
    val teamCount: Int,
    val teamSize: Int,
    val budget: Int? = null,
    val draftOrderStrategy: DraftOrderStrategy? = null,
    val currentTurnIndex: Int? = null,
    val currentAuctionRound: Int? = null,
    val players: List<RoomPlayer> = emptyList(),
    val leaders: List<RoomTeamLeader> = emptyList(),
    val members: List<RoomTeamMember> = emptyList(),
    val bids: List<RoomBid> = emptyList(),
    val createdAt: Instant = Instant.now(),
    val updatedAt: Instant = Instant.now(),
) : AggregateRoot<Room, RoomId> {
    @Transient
    private val pendingEvents: MutableList<Any> = mutableListOf()

    override fun getId(): RoomId = RoomId(roomId)

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

    internal fun join(
        nickname: String,
        teamLeaderId: String = UUID.randomUUID().toString(),
    ): Room {
        val leader = join(teamLeaderId = teamLeaderId, nickname = nickname, currentLeaderCount = leaders.size)
        return copy(leaders = leaders + leader)
            .registerEvent(
                RoomJoined(
                    roomId = roomId,
                    code = code,
                    leader =
                        LeaderSnapshot(
                            teamLeaderId = leader.teamLeaderId,
                            nickname = leader.nickname,
                            remainingBudget = leader.remainingBudget,
                        ),
                ),
            )
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

    internal fun start(): Room {
        val startedRoom = start(leaders.size)
        return startedRoom.registerEvent(
            RoomStarted(
                roomId = startedRoom.roomId,
                code = startedRoom.code,
                status = startedRoom.status,
                mode =
                    when (startedRoom.mode) {
                        TeamBuildingMode.AUCTION -> RoomStarted.Mode.AUCTION
                        TeamBuildingMode.DRAFT -> RoomStarted.Mode.DRAFT
                    },
            ),
        )
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

    internal fun placeBid(
        teamLeaderId: String,
        amount: Int,
    ): Room {
        check(isInProgress()) { "진행 중인 방에서만 가능합니다" }
        check(isAuction()) { "경매 모드가 아닙니다" }

        val currentRound = requireCurrentAuctionRound()
        val leader = leaders.firstOrNull { it.teamLeaderId == teamLeaderId } ?: throw RoomException.TeamLeaderNotFoundException()
        leader.requireCanBid(amount)

        val highest = bids.filter { it.round == currentRound }.maxByOrNull { it.amount }
        AuctionRound(round = currentRound, highestBid = highest).requireHigherBid(amount)

        val bid =
            RoomBid(
                roomId = roomId,
                round = currentRound,
                teamLeaderId = teamLeaderId,
                amount = amount,
            )

        return copy(bids = bids + bid)
    }

    internal fun settleAuction(): Room {
        check(isInProgress()) { "진행 중인 방에서만 가능합니다" }
        check(isAuction()) { "경매 모드가 아닙니다" }

        val currentRound = requireCurrentAuctionRound()
        val target = players.filter { it.status == PlayerStatus.AVAILABLE }.minByOrNull { it.displayOrder }
        requireNotNull(target) { "경매할 선수가 없습니다" }

        val highest = bids.filter { it.round == currentRound }.maxByOrNull { it.amount }
        val assignedCountAfterSettlement = members.size + 1
        val totalRequired = teamCount * picksPerTeam
        val settlement =
            AuctionRound(round = currentRound, highestBid = highest).settle(
                playerName = target.name,
                assignedCountAfterSettlement = assignedCountAfterSettlement,
                totalRequired = totalRequired,
            )

        return if (settlement.outcome == AuctionOutcome.SOLD) {
            settleSold(target, settlement)
        } else {
            settlePassed(target, settlement)
        }
    }

    internal fun pick(
        teamLeaderId: String,
        playerName: String,
    ): Room {
        check(isInProgress()) { "진행 중인 방에서만 가능합니다" }
        check(isDraft()) { "드래프트 모드가 아닙니다" }

        val turnIndex = requireCurrentTurnIndex()
        val strategy = requireNotNull(draftOrderStrategy) { "드래프트 모드에는 순서 전략이 필요합니다" }
        val draftBoard =
            DraftBoard(
                teamLeaderIds = leaders.map { it.teamLeaderId },
                strategy = strategy,
                picksPerTeam = picksPerTeam,
            )
        draftBoard.requireTurnOwner(turnIndex = turnIndex, teamLeaderId = teamLeaderId)

        leaders.firstOrNull { it.teamLeaderId == teamLeaderId } ?: throw RoomException.TeamLeaderNotFoundException()

        val target = players.firstOrNull { it.name == playerName && it.status == PlayerStatus.AVAILABLE }
        requireNotNull(target) { "선수 '$playerName'은(는) 선택할 수 없습니다" }

        val assignedCount = members.size
        val settlement = draftBoard.settlePick(turnIndex = turnIndex, assignedCountAfterPick = assignedCount + 1)
        val nextRoom = advanceDraftTurn(nextTurnIndex = settlement.nextTurnIndex, completed = settlement.completed)
        val assignedPlayer = target.assign()
        val member =
            RoomTeamMember(
                roomId = roomId,
                teamLeaderId = teamLeaderId,
                playerName = playerName,
                assignOrder = assignedCount,
            )

        val updatedRoom =
            nextRoom.copy(
                players = players.replacePlayerById(assignedPlayer.roomPlayerId, assignedPlayer),
                members = members + member,
            )

        val events =
            buildList {
                add(
                    DraftPickCompleted(
                        roomId = updatedRoom.roomId,
                        code = updatedRoom.code,
                        playerName = playerName,
                        teamLeaderId = teamLeaderId,
                    ),
                )
                if (updatedRoom.status == RoomStatus.COMPLETED) {
                    add(
                        RoomCompleted(
                            roomId = updatedRoom.roomId,
                            code = updatedRoom.code,
                            status = updatedRoom.status,
                            mode = RoomStarted.Mode.DRAFT,
                        ),
                    )
                }
            }

        return updatedRoom.registerEvents(events)
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

        internal fun createFromTemplate(
            code: String,
            hostId: String,
            hostNickname: String,
            template: TemplateBlueprint,
        ): Room {
            val room =
                when (template.mode) {
                    TemplateMode.AUCTION ->
                        createAuction(
                            code = code,
                            hostId = hostId,
                            teamCount = template.teamCount,
                            teamSize = template.teamSize,
                            budget = requireNotNull(template.budget) { "경매 템플릿에는 예산이 필요합니다" },
                        )

                    TemplateMode.DRAFT ->
                        createDraft(
                            code = code,
                            hostId = hostId,
                            teamCount = template.teamCount,
                            teamSize = template.teamSize,
                            draftOrderStrategy =
                                DraftOrderStrategy.valueOf(
                                    requireNotNull(template.draftOrderStrategy) {
                                        "드래프트 템플릿에는 순서 전략이 필요합니다"
                                    }.name,
                                ),
                        )
                }

            return room.copy(
                players =
                    template.players
                        .sortedBy { it.displayOrder }
                        .map { RoomPlayer(roomId = room.roomId, name = it.name, displayOrder = it.displayOrder) },
                leaders = listOf(room.createHostLeader(hostNickname)),
            )
        }
    }

    private fun settleSold(
        target: RoomPlayer,
        settlement: AuctionRoundSettlement,
    ): Room {
        val winningBid = requireNotNull(settlement.winningBid) { "낙찰 정산에는 최고 입찰이 필요합니다" }
        val winner = leaders.firstOrNull { it.teamLeaderId == winningBid.teamLeaderId } ?: throw RoomException.TeamLeaderNotFoundException()

        val leaderMemberCount = members.count { it.teamLeaderId == winningBid.teamLeaderId }
        AuctionRound(round = winningBid.round, highestBid = winningBid).requireRosterCapacity(
            currentMemberCount = leaderMemberCount,
            picksPerTeam = picksPerTeam,
        )

        val assignedCount = members.size
        val nextRoom = advanceAuction(nextRound = settlement.nextRound, completed = settlement.completed)
        val assignedPlayer = target.assign()
        val updatedWinner = winner.spend(winningBid.amount)
        val member =
            RoomTeamMember(
                roomId = roomId,
                teamLeaderId = winningBid.teamLeaderId,
                playerName = target.name,
                assignOrder = assignedCount,
            )

        val updatedRoom =
            nextRoom.copy(
                players = players.replacePlayerById(assignedPlayer.roomPlayerId, assignedPlayer),
                leaders = leaders.replaceLeaderById(updatedWinner.roomTeamLeaderId, updatedWinner),
                members = members + member,
            )

        val events =
            buildList {
                add(
                    AuctionSettled(
                        roomId = updatedRoom.roomId,
                        code = updatedRoom.code,
                        playerName = target.name,
                        outcome = AuctionOutcome.SOLD,
                        leaders = updatedRoom.leaderSnapshots(),
                    ),
                )
                if (updatedRoom.status == RoomStatus.COMPLETED) {
                    add(
                        RoomCompleted(
                            roomId = updatedRoom.roomId,
                            code = updatedRoom.code,
                            status = updatedRoom.status,
                            mode = RoomStarted.Mode.AUCTION,
                        ),
                    )
                }
            }

        return updatedRoom.registerEvents(events)
    }

    private fun settlePassed(
        target: RoomPlayer,
        settlement: AuctionRoundSettlement,
    ): Room {
        val nextRoom = moveAuctionTargetToNextRound(nextRound = settlement.nextRound)
        val maxOrder = players.maxOf { it.displayOrder }
        val movedTarget = target.moveToBack(maxOrder + 1)
        val updatedRoom = nextRoom.copy(players = players.replacePlayerById(movedTarget.roomPlayerId, movedTarget))

        return updatedRoom.registerEvent(
            AuctionSettled(
                roomId = updatedRoom.roomId,
                code = updatedRoom.code,
                playerName = settlement.playerName,
                outcome = settlement.outcome,
                leaders = updatedRoom.leaderSnapshots(),
            ),
        )
    }

    internal fun recordCreated(): Room {
        val hostLeader = leaders.single()
        return registerEvent(
            RoomCreated(
                roomId = roomId,
                code = code,
                status = status,
                hostLeader =
                    LeaderSnapshot(
                        teamLeaderId = hostLeader.teamLeaderId,
                        nickname = hostLeader.nickname,
                        remainingBudget = hostLeader.remainingBudget,
                    ),
            ),
        )
    }

    internal fun pendingEvents(): List<Any> = pendingEvents.toList()

    internal fun drainEvents(): List<Any> = pendingEvents.toList().also { pendingEvents.clear() }

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

    private fun leaderSnapshots(): List<LeaderSnapshot> =
        leaders.map { leader ->
            LeaderSnapshot(
                teamLeaderId = leader.teamLeaderId,
                nickname = leader.nickname,
                remainingBudget = leader.remainingBudget,
            )
        }

    private fun registerEvent(event: Any): Room = apply { pendingEvents += event }

    private fun registerEvents(events: Collection<Any>): Room = apply { pendingEvents.addAll(events) }

    internal fun restorePendingEvents(events: Collection<Any>): Room = apply { pendingEvents.addAll(events) }
}

fun Room.isWaiting(): Boolean = status == RoomStatus.WAITING

fun Room.isInProgress(): Boolean = status == RoomStatus.IN_PROGRESS

fun Room.isAuction(): Boolean = mode == TeamBuildingMode.AUCTION

fun Room.isDraft(): Boolean = mode == TeamBuildingMode.DRAFT

val Room.configuration: TeamBuildingConfiguration
    get() = TeamBuildingConfiguration.from(this)

val Room.progress: RoomProgress
    get() = RoomProgress.from(this)

val Room.picksPerTeam: Int
    get() = teamSize - 1

private fun List<RoomPlayer>.replacePlayerById(
    roomPlayerId: Long,
    replacement: RoomPlayer,
): List<RoomPlayer> = map { if (it.roomPlayerId == roomPlayerId) replacement else it }

private fun List<RoomTeamLeader>.replaceLeaderById(
    roomTeamLeaderId: Long,
    replacement: RoomTeamLeader,
): List<RoomTeamLeader> = map { if (it.roomTeamLeaderId == roomTeamLeaderId) replacement else it }
