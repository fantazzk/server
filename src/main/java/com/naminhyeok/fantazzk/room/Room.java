package com.naminhyeok.fantazzk.room;

import com.naminhyeok.fantazzk.CoreException;
import jakarta.persistence.Access;
import jakarta.persistence.AccessType;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Locale;
import java.util.UUID;
import lombok.Getter;
import org.jmolecules.ddd.types.AggregateRoot;

@Getter
@Access(AccessType.FIELD)
@Table(name = "rooms")
class Room implements AggregateRoot<Room, RoomId> {
    private final RoomId id;
    private final String code;
    @Version
    private long version;
    @Column(nullable = false, updatable = false)
    private final Instant createdAt;
    @Column(name = "host_id")
    @Convert(converter = TeamLeaderId.JpaConverter.class)
    private final TeamLeaderId hostLeaderId;
    @Enumerated(EnumType.STRING)
    private RoomStatus status;
    @Enumerated(EnumType.STRING)
    private final RoomMode mode;
    private final int teamCount;
    private final int teamSize;
    private final Integer budget;
    @Column(name = "pick_ban_time")
    private final int pickBanTime;
    private final Integer minBidUnit;
    @Column(name = "position_limit")
    private final Integer positionLimit;
    @Enumerated(EnumType.STRING)
    private final DraftOrderStrategy draftOrderStrategy;
    @Column(name = "started_game_id")
    @Convert(converter = GameId.JpaConverter.class)
    private GameId startedGameId;
    @Column(name = "started_at")
    private Instant startedAt;
    @ElementCollection
    @CollectionTable(name = "room_player", joinColumns = @JoinColumn(name = "players_room_id"))
    private final List<RoomPlayer> players;
    @ElementCollection
    @CollectionTable(name = "room_team_leader", joinColumns = @JoinColumn(name = "leaders_room_id"))
    private final List<RoomTeamLeader> leaders;

    Room(
        String code,
        Instant createdAt,
        TeamLeaderId hostLeaderId,
        RoomMode mode,
        int teamCount,
        int teamSize,
        Integer budget,
        int pickBanTime,
        Integer minBidUnit,
        Integer positionLimit,
        DraftOrderStrategy draftOrderStrategy
    ) {
        this.id = new RoomId(UUID.randomUUID());
        this.code = code;
        this.createdAt = Objects.requireNonNull(createdAt, "createdAt must not be null");
        this.hostLeaderId = Objects.requireNonNull(hostLeaderId, "hostLeaderId must not be null");
        this.status = RoomStatus.WAITING;
        this.mode = mode;
        this.teamCount = teamCount;
        this.teamSize = teamSize;
        this.budget = budget;
        this.pickBanTime = pickBanTime;
        this.minBidUnit = minBidUnit;
        this.positionLimit = positionLimit;
        this.draftOrderStrategy = draftOrderStrategy;
        this.startedGameId = null;
        this.startedAt = null;
        this.players = new ArrayList<>();
        this.leaders = new ArrayList<>();
    }

    public static Room createFromTemplate(
        String code,
        TeamLeaderId hostLeaderId,
        String hostNickname,
        String hostActionToken,
        RoomTemplateSpec spec,
        Instant createdAt
    ) {
        Room room =
            new Room(
                code,
                createdAt,
                hostLeaderId,
                spec.mode() == RoomMode.AUCTION ? RoomMode.AUCTION : RoomMode.DRAFT,
                spec.teamCount(),
                spec.teamSize(),
                spec.budget(),
                spec.pickBanTime(),
                spec.minBidUnit(),
                spec.positionLimit(),
                spec.draftOrderStrategy()
            );

        spec.players().stream()
            .sorted(Comparator.comparingInt(RoomTemplateSpec.Player::displayOrder))
            .map(player -> new RoomPlayer(player.id(), player.name(), player.position(), player.displayOrder()))
            .forEach(room.players::add);

        room.leaders.add(new RoomTeamLeader(hostLeaderId, hostNickname.trim(), hostActionToken, spec.budget()));
        return room;
    }

    public List<RoomPlayer> getPlayers() {
        return players.stream().sorted(Comparator.comparingInt(RoomPlayer::getDisplayOrder)).toList();
    }

    public List<RoomTeamLeader> getLeaders() {
        return List.copyOf(leaders);
    }

    public RoomStartReadiness getStartReadiness() {
        if (status != RoomStatus.WAITING) {
            return RoomStartReadiness.NOT_WAITING;
        }
        if (leaders.size() != teamCount) {
            return RoomStartReadiness.WAITING_FOR_LEADERS;
        }
        if (mode == RoomMode.DRAFT && !hasConfirmedDraftPositions()) {
            return RoomStartReadiness.WAITING_FOR_DRAFT_POSITIONS;
        }
        return RoomStartReadiness.STARTABLE;
    }

    boolean isJoinable() {
        return status == RoomStatus.WAITING && leaders.size() < teamCount;
    }

    public void join(TeamLeaderId teamLeaderId, String nickname, String actionToken) {
        if (status != RoomStatus.WAITING) {
            throw CoreException.of(RoomErrorType.ROOM_JOIN_REQUIRES_WAITING);
        }
        if (leaders.size() >= teamCount) {
            throw CoreException.of(RoomErrorType.ROOM_FULL);
        }

        String normalizedNickname = normalizeNickname(nickname);
        boolean taken =
            leaders.stream()
                .map(RoomTeamLeader::getNickname)
                .map(this::normalizeNickname)
                .anyMatch(normalizedNickname::equals);
        if (taken) {
            throw CoreException.of(RoomErrorType.ROOM_NICKNAME_ALREADY_TAKEN);
        }

        leaders.add(new RoomTeamLeader(teamLeaderId, nickname.trim(), actionToken, budget));
    }

    private String normalizeNickname(String nickname) {
        return nickname.trim().toLowerCase(Locale.ROOT);
    }

    void selectDraftPosition(TeamLeaderId callerLeaderId, int draftPosition) {
        validateDraftPositionChange(draftPosition);

        RoomTeamLeader caller = getLeader(callerLeaderId);
        boolean taken =
            leaders.stream()
                .filter(leader -> !leader.getId().equals(callerLeaderId))
                .anyMatch(leader -> Integer.valueOf(draftPosition).equals(leader.getDraftPosition()));
        if (taken) {
            throw CoreException.of(RoomErrorType.ROOM_DRAFT_POSITION_TAKEN);
        }

        caller.assignDraftPosition(draftPosition);
    }

    void clearDraftPosition(TeamLeaderId callerLeaderId) {
        validateDraftModeWaiting();
        getLeader(callerLeaderId).clearDraftPosition();
    }

    void start(TeamLeaderId callerLeaderId, Instant now) {
        start(callerLeaderId, new GameId(UUID.randomUUID()), now);
    }

    StartedGameSnapshot start(TeamLeaderId callerLeaderId, GameId gameId, Instant now) {
        if (!hostLeaderId.equals(callerLeaderId)) {
            throw CoreException.of(RoomErrorType.ROOM_START_FORBIDDEN);
        }
        RoomStartReadiness readiness = getStartReadiness();
        if (readiness == RoomStartReadiness.NOT_WAITING) {
            throw CoreException.of(RoomErrorType.ROOM_START_REQUIRES_WAITING);
        }
        if (readiness == RoomStartReadiness.WAITING_FOR_LEADERS) {
            throw CoreException.of(RoomErrorType.ROOM_LEADERS_NOT_FULL);
        }
        if (readiness == RoomStartReadiness.WAITING_FOR_DRAFT_POSITIONS) {
            throw CoreException.of(RoomErrorType.ROOM_DRAFT_POSITIONS_NOT_FULL);
        }

        startedGameId = Objects.requireNonNull(gameId, "gameId must not be null");
        startedAt = Objects.requireNonNull(now, "now must not be null");
        status = RoomStatus.STARTED;

        return new StartedGameSnapshot(
            id,
            code,
            startedGameId,
            startedAt,
            mode,
            mode == RoomMode.AUCTION
                ? GameRules.auction(teamCount, teamSize, budget, pickBanTime, minBidUnit, positionLimit)
                : GameRules.draft(teamCount, teamSize, pickBanTime, draftOrderStrategy),
            leaders.stream()
                .map(leader -> mode == RoomMode.AUCTION
                    ? GameParticipant.auction(leader.getId(), leader.getNickname(), leader.getRemainingBudget())
                    : GameParticipant.draft(leader.getId(), leader.getNickname(), leader.getDraftPosition()))
                .toList(),
            players.stream()
                .sorted(Comparator.comparingInt(RoomPlayer::getDisplayOrder))
                .map(player -> new GamePlayer(player.getId(), player.getName(), player.getPosition(), player.getDisplayOrder()))
                .toList()
        );
    }

    private void validateDraftPositionChange(int draftPosition) {
        validateDraftModeWaiting();
        if (draftPosition < 1 || draftPosition > teamCount) {
            throw CoreException.of(RoomErrorType.ROOM_DRAFT_POSITION_OUT_OF_RANGE);
        }
    }

    private void validateDraftModeWaiting() {
        if (mode != RoomMode.DRAFT) {
            throw CoreException.of(RoomErrorType.ROOM_DRAFT_POSITION_REQUIRES_DRAFT_MODE);
        }
        if (status != RoomStatus.WAITING) {
            throw CoreException.of(RoomErrorType.ROOM_DRAFT_POSITION_REQUIRES_WAITING);
        }
    }

    private boolean hasConfirmedDraftPositions() {
        return leaders.stream().allMatch(leader -> leader.getDraftPosition() != null)
            && leaders.stream().map(RoomTeamLeader::getDraftPosition).distinct().count() == teamCount;
    }

    private RoomTeamLeader getLeader(TeamLeaderId teamLeaderId) {
        return leaders.stream()
            .filter(leader -> leader.getId().equals(teamLeaderId))
            .findFirst()
            .orElseThrow(() -> RoomStateInvalidException.leaderMissing(teamLeaderId));
    }
}
