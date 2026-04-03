package com.naminhyeok.fantazzk.room
import com.naminhyeok.fantazzk.room.exception.RoomException
import jakarta.persistence.CascadeType
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.OneToMany
import jakarta.persistence.OrderBy
import jakarta.persistence.PostLoad
import jakarta.persistence.Table
import org.jmolecules.ddd.types.AggregateRoot
import java.time.Instant
import java.util.UUID

@Entity
@Table(name = "room")
class Room protected constructor(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private var persistentId: Long? = null,
    @Column(name = "code", nullable = false)
    val code: String = "",
    @Column(name = "host_id", nullable = false)
    val hostId: String = "",
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    var status: RoomStatus = RoomStatus.WAITING,
    @Enumerated(EnumType.STRING)
    @Column(name = "mode", nullable = false)
    val mode: TeamBuildingMode = TeamBuildingMode.AUCTION,
    @Column(name = "team_count", nullable = false)
    val teamCount: Int = 1,
    @Column(name = "team_size", nullable = false)
    val teamSize: Int = 2,
    @Column(name = "budget")
    val budget: Int? = 1,
    @Enumerated(EnumType.STRING)
    @Column(name = "draft_order_strategy")
    var draftOrderStrategy: DraftOrderStrategy? = null,
    @Column(name = "current_turn_index")
    var currentTurnIndex: Int? = null,
    @Column(name = "current_auction_round")
    var currentAuctionRound: Int? = null,
    @OneToMany(mappedBy = "room", cascade = [CascadeType.ALL], orphanRemoval = true)
    @OrderBy("displayOrder ASC")
    private val persistentPlayers: MutableList<RoomPlayer> = mutableListOf(),
    @OneToMany(mappedBy = "room", cascade = [CascadeType.ALL], orphanRemoval = true)
    @OrderBy("roomTeamLeaderId ASC")
    private val persistentLeaders: MutableList<RoomTeamLeader> = mutableListOf(),
    @OneToMany(mappedBy = "room", cascade = [CascadeType.ALL], orphanRemoval = true)
    @OrderBy("assignOrder ASC")
    private val persistentMembers: MutableList<RoomTeamMember> = mutableListOf(),
    @OneToMany(mappedBy = "room", cascade = [CascadeType.ALL])
    @OrderBy("roomBidId ASC")
    private val persistentBids: MutableList<RoomBid> = mutableListOf(),
    @Column(name = "created_at", nullable = false)
    val createdAt: Instant = Instant.now(),
    @Column(name = "updated_at", nullable = false)
    val updatedAt: Instant = Instant.now(),
) : AggregateRoot<Room, RoomId> {
    init {
        validateState()
    }

    constructor(
        roomId: Long = 0L,
        code: String,
        hostId: String,
        status: RoomStatus,
        mode: TeamBuildingMode,
        teamCount: Int,
        teamSize: Int,
        budget: Int? = null,
        draftOrderStrategy: DraftOrderStrategy? = null,
        currentTurnIndex: Int? = null,
        currentAuctionRound: Int? = null,
        players: List<RoomPlayer> = emptyList(),
        leaders: List<RoomTeamLeader> = emptyList(),
        members: List<RoomTeamMember> = emptyList(),
        bids: List<RoomBid> = emptyList(),
        createdAt: Instant = Instant.now(),
        updatedAt: Instant = Instant.now(),
    ) : this(
        persistentId = roomId.takeIf { it != 0L },
        code = code,
        hostId = hostId,
        status = status,
        mode = mode,
        teamCount = teamCount,
        teamSize = teamSize,
        budget = budget,
        draftOrderStrategy = draftOrderStrategy,
        currentTurnIndex = currentTurnIndex,
        currentAuctionRound = currentAuctionRound,
        createdAt = createdAt,
        updatedAt = updatedAt,
    ) {
        registerPlayers(players)
        registerLeaders(leaders)
        registerMembers(members)
        registerBids(bids)
    }

    override fun getId(): RoomId = RoomId(requireNotNull(persistentId))

    val roomId: Long
        get() = persistentId ?: 0L

    val players: List<RoomPlayer>
        get() = persistentPlayers.toList()

    val leaders: List<RoomTeamLeader>
        get() = persistentLeaders.toList()

    val members: List<RoomTeamMember>
        get() = persistentMembers.toList()

    val bids: List<RoomBid>
        get() = currentAuctionRound?.let { round -> persistentBids.filter { it.round == round } }.orEmpty()

    internal fun bidHistory(): List<RoomBid> = persistentBids.toList()

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
        addLeader(leader)
        return this
    }

    fun start(leaderCount: Int): Room {
        check(isWaiting()) { "대기 중인 방에서만 시작할 수 있습니다" }
        check(leaderCount == teamCount) { "모든 팀장 자리가 채워져야 시작할 수 있습니다" }

        when (configuration) {
            is TeamBuildingConfiguration.Auction -> {
                status = RoomStatus.IN_PROGRESS
                currentAuctionRound = 1
                currentTurnIndex = null
            }

            is TeamBuildingConfiguration.Draft -> {
                status = RoomStatus.IN_PROGRESS
                currentTurnIndex = 0
                currentAuctionRound = null
            }
        }
        return this
    }

    internal fun start(): Room {
        start(leaders.size)
        return this
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
        currentAuctionRound = nextRound
        currentTurnIndex = null
        if (completed) {
            status = RoomStatus.COMPLETED
        }
        return this
    }

    fun moveAuctionTargetToNextRound(nextRound: Int): Room {
        check(isAuction()) { "경매 모드가 아닙니다" }
        check(isInProgress()) { "진행 중인 방에서만 가능합니다" }
        requireNextAuctionRound(nextRound)
        currentAuctionRound = nextRound
        currentTurnIndex = null
        return this
    }

    fun advanceDraftTurn(
        nextTurnIndex: Int,
        completed: Boolean,
    ): Room {
        check(isDraft()) { "드래프트 모드가 아닙니다" }
        check(isInProgress()) { "진행 중인 방에서만 가능합니다" }
        requireNextDraftTurn(nextTurnIndex)
        currentTurnIndex = nextTurnIndex
        currentAuctionRound = null
        if (completed) {
            status = RoomStatus.COMPLETED
        }
        return this
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

        val highest = bids.maxByOrNull { it.amount }
        AuctionRound(round = currentRound, highestBid = highest).requireHigherBid(amount)

        addBid(
            RoomBid(
                round = currentRound,
                teamLeaderId = teamLeaderId,
                amount = amount,
            ),
        )

        return this
    }

    internal fun settleAuction(): Room {
        check(isInProgress()) { "진행 중인 방에서만 가능합니다" }
        check(isAuction()) { "경매 모드가 아닙니다" }

        val currentRound = requireCurrentAuctionRound()
        val target = players.filter { it.status == PlayerStatus.AVAILABLE }.minByOrNull { it.displayOrder }
        requireNotNull(target) { "경매할 선수가 없습니다" }

        val highest = bids.maxByOrNull { it.amount }
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
        advanceDraftTurn(nextTurnIndex = settlement.nextTurnIndex, completed = settlement.completed)
        target.assign()
        addMember(
            RoomTeamMember(
                teamLeaderId = teamLeaderId,
                playerName = playerName,
                assignOrder = assignedCount,
            ),
        )

        return this
    }

    internal fun assignId(roomId: RoomId): Room = apply { persistentId = roomId.value }

    internal fun detachCopy(): Room =
        Room(
            roomId = roomId,
            code = code,
            hostId = hostId,
            status = status,
            mode = mode,
            teamCount = teamCount,
            teamSize = teamSize,
            budget = budget,
            draftOrderStrategy = draftOrderStrategy,
            currentTurnIndex = currentTurnIndex,
            currentAuctionRound = currentAuctionRound,
            players = players.map(RoomPlayer::detachCopy),
            leaders = leaders.map(RoomTeamLeader::detachCopy),
            members = members.map(RoomTeamMember::detachCopy),
            bids = bidHistory().map(RoomBid::detachCopy),
            createdAt = createdAt,
            updatedAt = updatedAt,
        )

    fun copy(
        roomId: Long = this.roomId,
        code: String = this.code,
        hostId: String = this.hostId,
        status: RoomStatus = this.status,
        mode: TeamBuildingMode = this.mode,
        teamCount: Int = this.teamCount,
        teamSize: Int = this.teamSize,
        budget: Int? = this.budget,
        draftOrderStrategy: DraftOrderStrategy? = this.draftOrderStrategy,
        currentTurnIndex: Int? = this.currentTurnIndex,
        currentAuctionRound: Int? = this.currentAuctionRound,
        players: List<RoomPlayer> = this.players.map(RoomPlayer::detachCopy),
        leaders: List<RoomTeamLeader> = this.leaders.map(RoomTeamLeader::detachCopy),
        members: List<RoomTeamMember> = this.members.map(RoomTeamMember::detachCopy),
        bids: List<RoomBid> = this.bidHistory().map(RoomBid::detachCopy),
        createdAt: Instant = this.createdAt,
        updatedAt: Instant = this.updatedAt,
    ): Room =
        Room(
            roomId = roomId,
            code = code,
            hostId = hostId,
            status = status,
            mode = mode,
            teamCount = teamCount,
            teamSize = teamSize,
            budget = budget,
            draftOrderStrategy = draftOrderStrategy,
            currentTurnIndex = currentTurnIndex,
            currentAuctionRound = currentAuctionRound,
            players = players,
            leaders = leaders,
            members = members,
            bids = bids,
            createdAt = createdAt,
            updatedAt = updatedAt,
        )

    @PostLoad
    private fun validateLoadedState() {
        validateState()
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
                roomId = 0L,
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
                roomId = 0L,
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
            spec: RoomTemplateSpec,
        ): Room {
            val room =
                when (spec.mode) {
                    RoomTemplateSpec.Mode.AUCTION ->
                        createAuction(
                            code = code,
                            hostId = hostId,
                            teamCount = spec.teamCount,
                            teamSize = spec.teamSize,
                            budget = requireNotNull(spec.budget) { "경매 템플릿에는 예산이 필요합니다" },
                        )

                    RoomTemplateSpec.Mode.DRAFT ->
                        createDraft(
                            code = code,
                            hostId = hostId,
                            teamCount = spec.teamCount,
                            teamSize = spec.teamSize,
                            draftOrderStrategy =
                                DraftOrderStrategy.valueOf(
                                    requireNotNull(spec.draftOrderStrategy) {
                                        "드래프트 템플릿에는 순서 전략이 필요합니다"
                                    }.name,
                                ),
                        )
                }

            spec.players
                .sortedBy { it.displayOrder }
                .map { RoomPlayer(name = it.name, displayOrder = it.displayOrder) }
                .forEach(room::addPlayer)
            room.addLeader(room.createHostLeader(hostNickname))
            return room
        }

        internal fun reference(roomId: Long): Room = Room(persistentId = roomId)
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
        advanceAuction(nextRound = settlement.nextRound, completed = settlement.completed)
        target.assign()
        winner.spend(winningBid.amount)
        addMember(
            RoomTeamMember(
                teamLeaderId = winningBid.teamLeaderId,
                playerName = target.name,
                assignOrder = assignedCount,
            ),
        )

        return this
    }

    private fun settlePassed(
        target: RoomPlayer,
        settlement: AuctionRoundSettlement,
    ): Room {
        moveAuctionTargetToNextRound(nextRound = settlement.nextRound)
        val maxOrder = players.maxOf { it.displayOrder }
        target.moveToBack(maxOrder + 1)

        return this
    }

    private fun createTeamLeader(
        teamLeaderId: String,
        nickname: String,
    ): RoomTeamLeader =
        RoomTeamLeader(
            teamLeaderId = teamLeaderId,
            nickname = nickname,
            remainingBudget =
                when (val configuration = configuration) {
                    is TeamBuildingConfiguration.Auction -> configuration.budget
                    is TeamBuildingConfiguration.Draft -> null
                },
        ).also { it.attach(this) }

    private fun registerPlayers(players: List<RoomPlayer>) {
        persistentPlayers.clear()
        players.forEach(::addPlayer)
    }

    private fun registerLeaders(leaders: List<RoomTeamLeader>) {
        persistentLeaders.clear()
        leaders.forEach(::addLeader)
    }

    private fun registerMembers(members: List<RoomTeamMember>) {
        persistentMembers.clear()
        members.forEach(::addMember)
    }

    private fun registerBids(bids: List<RoomBid>) {
        persistentBids.clear()
        bids.forEach(::addBid)
    }

    private fun addPlayer(player: RoomPlayer) {
        player.detachCopy().also {
            it.attach(this)
            persistentPlayers += it
        }
    }

    private fun addLeader(leader: RoomTeamLeader) {
        leader.detachCopy().also {
            it.attach(this)
            persistentLeaders += it
        }
    }

    private fun addMember(member: RoomTeamMember) {
        member.detachCopy().also {
            it.attach(this)
            persistentMembers += it
        }
    }

    private fun addBid(bid: RoomBid) {
        bid.detachCopy().also {
            it.attach(this)
            persistentBids += it
        }
    }

    private fun requireNextAuctionRound(nextRound: Int) {
        val currentRound = requireCurrentAuctionRound()
        require(nextRound > currentRound) { "다음 경매 라운드는 현재보다 커야 합니다" }
    }

    private fun requireNextDraftTurn(nextTurnIndex: Int) {
        val currentIndex = requireCurrentTurnIndex()
        require(nextTurnIndex > currentIndex) { "다음 드래프트 턴은 현재보다 커야 합니다" }
    }

    private fun validateState() {
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
