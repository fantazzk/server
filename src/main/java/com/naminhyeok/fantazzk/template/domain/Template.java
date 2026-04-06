package com.naminhyeok.fantazzk.template.domain;

import com.naminhyeok.fantazzk.template.TemplateId;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.PostLoad;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import org.jmolecules.ddd.types.AggregateRoot;
import org.jetbrains.annotations.Nullable;

@Entity
@Table(name = "template")
public class Template implements AggregateRoot<Template, TemplateId> {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id")
    private UUID persistentId;

    @Column(name = "name", nullable = false)
    private String name;

    @Embedded
    private TemplateConfiguration persistentConfiguration;

    @OneToMany(mappedBy = "template", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("displayOrder ASC")
    private final List<TemplatePlayer> persistentPlayers = new ArrayList<>();

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    protected Template() {
        this(null, "", TemplateConfiguration.auction(1, 2, 1), Instant.now(), Instant.now());
    }

    private Template(
        UUID persistentId,
        String name,
        TemplateConfiguration persistentConfiguration,
        Instant createdAt,
        Instant updatedAt
    ) {
        this.persistentId = persistentId;
        this.name = Objects.requireNonNull(name, "name must not be null");
        this.persistentConfiguration = Objects.requireNonNull(persistentConfiguration, "persistentConfiguration must not be null");
        this.createdAt = Objects.requireNonNull(createdAt, "createdAt must not be null");
        this.updatedAt = Objects.requireNonNull(updatedAt, "updatedAt must not be null");
    }

    @Override
    public TemplateId getId() {
        return getTemplateId();
    }

    public TemplateId getTemplateId() {
        return new TemplateId(Objects.requireNonNull(persistentId, "templateId is not assigned"));
    }

    public String getName() {
        return name;
    }

    public TeamBuildingMode getMode() {
        return persistentConfiguration.getMode();
    }

    public int getTeamCount() {
        return persistentConfiguration.getTeamCount();
    }

    public int getTeamSize() {
        return persistentConfiguration.getTeamSize();
    }

    @Nullable
    public Integer getBudget() {
        return persistentConfiguration.getBudget();
    }

    @Nullable
    public DraftOrderStrategy getDraftOrderStrategy() {
        return persistentConfiguration.getDraftOrderStrategy();
    }

    public TemplateConfiguration getConfiguration() {
        return TemplateConfiguration.from(
            getMode(),
            getTeamCount(),
            getTeamSize(),
            getBudget(),
            getDraftOrderStrategy()
        );
    }

    public int getPicksPerTeam() {
        return getTeamSize() - 1;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public List<TemplatePlayer> players() {
        List<TemplatePlayer> players = List.copyOf(persistentPlayers);
        requireValidRoster(players);
        return players;
    }

    public void requireValidRoster(List<TemplatePlayer> players) {
        List<String> orderedPlayerNames = players.stream()
            .sorted(Comparator.comparingInt(TemplatePlayer::getDisplayOrder))
            .map(TemplatePlayer::getName)
            .toList();
        TemplateRoster.exactlyRequired(orderedPlayerNames, getConfiguration().getRequiredPlayerCount());
    }

    public Template assignId(TemplateId templateId) {
        this.persistentId = Objects.requireNonNull(templateId, "templateId must not be null").getValue();
        return this;
    }

    @PostLoad
    private void validateLoadedState() {
        getConfiguration();
        players();
    }

    public static Template createAuction(
        String name,
        int teamCount,
        int teamSize,
        int budget,
        List<String> playerNames
    ) {
        return new Template(
            null,
            name,
            TemplateConfiguration.auction(teamCount, teamSize, budget),
            Instant.now(),
            Instant.now()
        ).registerPlayers(playerNames);
    }

    public static Template createDraft(
        String name,
        int teamCount,
        int teamSize,
        DraftOrderStrategy strategy,
        List<String> playerNames
    ) {
        return new Template(
            null,
            name,
            TemplateConfiguration.draft(teamCount, teamSize, strategy),
            Instant.now(),
            Instant.now()
        ).registerPlayers(playerNames);
    }

    static Template reference(TemplateId templateId) {
        return new Template(
            Objects.requireNonNull(templateId, "templateId must not be null").getValue(),
            "",
            TemplateConfiguration.auction(1, 2, 1),
            Instant.now(),
            Instant.now()
        );
    }

    private Template registerPlayers(List<String> playerNames) {
        Objects.requireNonNull(playerNames, "playerNames must not be null");
        persistentPlayers.clear();
        for (int index = 0; index < playerNames.size(); index++) {
            TemplatePlayer player = new TemplatePlayer(playerNames.get(index), index);
            player.attach(this);
            persistentPlayers.add(player);
        }
        requireValidRoster(List.copyOf(persistentPlayers));
        return this;
    }
}
