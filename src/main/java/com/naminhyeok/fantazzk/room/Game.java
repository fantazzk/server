package com.naminhyeok.fantazzk.room;

import jakarta.persistence.Access;
import jakarta.persistence.AccessType;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.DiscriminatorColumn;
import jakarta.persistence.DiscriminatorType;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Enumerated;
import jakarta.persistence.EnumType;
import jakarta.persistence.FetchType;
import jakarta.persistence.Inheritance;
import jakarta.persistence.InheritanceType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OrderColumn;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import jakarta.persistence.Version;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import lombok.Getter;
import org.jmolecules.ddd.types.AggregateRoot;
import org.jmolecules.event.types.DomainEvent;
import org.springframework.data.domain.AfterDomainEventPublication;
import org.springframework.data.domain.DomainEvents;

@Getter
@Access(AccessType.FIELD)
@Table(name = "games")
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "game_mode", discriminatorType = DiscriminatorType.STRING)
abstract class Game implements AggregateRoot<Game, GameId> {
    private final GameId id;
    @Version
    private long version;
    @Column(name = "room_id", nullable = false, updatable = false)
    @Convert(converter = RoomId.JpaConverter.class)
    private final RoomId roomId;
    @Column(name = "room_code", nullable = false, updatable = false)
    private final String roomCode;
    @Column(name = "started_at", nullable = false, updatable = false)
    private final Instant startedAt;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private GameStatus status;
    @Column(name = "team_count", nullable = false, updatable = false)
    private final int teamCount;
    @Column(name = "team_size", nullable = false, updatable = false)
    private final int teamSize;
    @Column(updatable = false)
    private final Integer budget;
    @Column(name = "pick_ban_time", nullable = false, updatable = false)
    private final int pickBanTime;
    @Column(name = "min_bid_unit", updatable = false)
    private final Integer minBidUnit;
    @Column(name = "position_limit", updatable = false)
    private final Integer positionLimit;
    @Enumerated(EnumType.STRING)
    @Column(name = "draft_order_strategy", updatable = false)
    private final DraftOrderStrategy draftOrderStrategy;
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "game_participant", joinColumns = @JoinColumn(name = "participants_game_id"))
    @OrderColumn(name = "participant_order")
    private final List<GameParticipant> participants;
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "game_player", joinColumns = @JoinColumn(name = "player_pool_game_id"))
    @OrderColumn(name = "player_pool_order")
    private final List<GamePlayer> playerPool;
    @Transient
    private List<DomainEvent> domainEvents;

    protected Game() {
        this.id = null;
        this.version = 0L;
        this.roomId = null;
        this.roomCode = null;
        this.startedAt = null;
        this.status = null;
        this.teamCount = 0;
        this.teamSize = 0;
        this.budget = null;
        this.pickBanTime = 0;
        this.minBidUnit = null;
        this.positionLimit = null;
        this.draftOrderStrategy = null;
        this.participants = new ArrayList<>();
        this.playerPool = new ArrayList<>();
        this.domainEvents = new ArrayList<>();
    }

    Game(
        GameId id,
        RoomId roomId,
        String roomCode,
        Instant startedAt,
        GameStatus status,
        GameRules rules,
        List<GameParticipant> participants,
        List<GamePlayer> playerPool
    ) {
        this.id = id;
        this.version = 0L;
        this.roomId = roomId;
        this.roomCode = roomCode;
        this.startedAt = startedAt;
        this.status = status;
        this.teamCount = rules.teamCount();
        this.teamSize = rules.teamSize();
        this.budget = rules.budget();
        this.pickBanTime = rules.pickBanTime();
        this.minBidUnit = rules.minBidUnit();
        this.positionLimit = rules.positionLimit();
        this.draftOrderStrategy = rules.draftOrderStrategy();
        this.participants = new ArrayList<>(participants);
        this.playerPool = new ArrayList<>(playerPool);
        this.domainEvents = new ArrayList<>();
    }

    abstract GameRules getRules();

    List<GameParticipant> getParticipants() {
        return List.copyOf(participants);
    }

    List<GamePlayer> getPlayerPool() {
        return List.copyOf(playerPool);
    }

    protected List<GameParticipant> mutableParticipants() {
        return participants;
    }

    protected List<GamePlayer> mutablePlayerPool() {
        return playerPool;
    }

    protected void changeStatus(GameStatus status) {
        this.status = status;
    }

    protected void registerEvent(DomainEvent event) {
        domainEvents.add(event);
    }

    @DomainEvents
    Collection<DomainEvent> domainEvents() {
        return List.copyOf(domainEvents);
    }

    @AfterDomainEventPublication
    void clearDomainEvents() {
        domainEvents = new ArrayList<>();
    }
}
