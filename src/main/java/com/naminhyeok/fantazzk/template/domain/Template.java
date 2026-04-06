package com.naminhyeok.fantazzk.template.domain;

import com.naminhyeok.fantazzk.template.TemplateId;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
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
import java.util.stream.Collectors;
import org.jmolecules.ddd.types.AggregateRoot;
import org.springframework.lang.Nullable;

@Entity
@Table(name = "template")
public class Template implements AggregateRoot<Template, TemplateId> {

    @Id
    @Column(name = "id", nullable = false, updatable = false)
    private TemplateId id;

    @Column(name = "name", nullable = false)
    private String name = "";

    @Embedded
    private TemplateConfiguration persistentConfiguration = TemplateConfiguration.auction(1, 2, 1);

    @OneToMany(mappedBy = "template", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("displayOrder ASC")
    private List<TemplatePlayer> persistentPlayers = new ArrayList<>();

    @Column(name = "created_at", nullable = false)
    private Instant createdAt = Instant.now();

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt = Instant.now();

    protected Template() {}

    protected Template(
            TemplateId id,
            String name,
            TemplateConfiguration configuration,
            List<TemplatePlayer> players,
            Instant createdAt,
            Instant updatedAt
    ) {
        this.id = Objects.requireNonNull(id, "id must not be null");
        this.name = Objects.requireNonNull(name, "name must not be null");
        this.persistentConfiguration = Objects.requireNonNull(configuration, "configuration must not be null");
        this.persistentPlayers = Objects.requireNonNull(players, "players must not be null");
        this.createdAt = Objects.requireNonNull(createdAt, "createdAt must not be null");
        this.updatedAt = Objects.requireNonNull(updatedAt, "updatedAt must not be null");
    }

    protected Template(TemplateId id) {
        this(
                id,
                "",
                TemplateConfiguration.auction(1, 2, 1),
                new ArrayList<>(),
                Instant.now(),
                Instant.now()
        );
    }

    protected Template(String name, TemplateConfiguration configuration) {
        this(TemplateId.newId(), name, configuration, new ArrayList<>(), Instant.now(), Instant.now());
    }

    public UUID getTemplateId() {
        return id.getValue();
    }

    public TemplateId getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public TemplateConfiguration getConfiguration() {
        return TemplateConfiguration.from(
                persistentConfiguration.getMode(),
                persistentConfiguration.getTeamCount(),
                persistentConfiguration.getTeamSize(),
                persistentConfiguration.getBudget(),
                persistentConfiguration.getDraftOrderStrategy()
        );
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
        List<TemplatePlayer> players = persistentPlayers.stream().toList();
        requireValidRoster(players);
        return players;
    }

    public void requireValidRoster(List<TemplatePlayer> players) {
        var invalidRoster = players.stream()
                .anyMatch(player -> !player.belongsTo(id));

        if (invalidRoster) {
            throw new IllegalArgumentException("선수는 동일한 템플릿에 속해야 합니다");
        }

        List<String> orderedPlayerNames = players
                .stream()
                .sorted(Comparator.comparingInt(TemplatePlayer::getDisplayOrder))
                .map(TemplatePlayer::getName)
                .collect(Collectors.toList());

        TemplateRoster.exactlyRequired(orderedPlayerNames, getConfiguration().getRequiredPlayerCount());
    }

    @PostLoad
    private void validateLoadedState() {
        getConfiguration();
    }

    public static Template createAuction(
            String name,
            int teamCount,
            int teamSize,
            int budget,
            List<String> playerNames
    ) {
        Template template = new Template(name, TemplateConfiguration.auction(teamCount, teamSize, budget));
        template.registerPlayers(playerNames).requireValidRoster(template.players());
        return template;
    }

    public static Template createDraft(
            String name,
            int teamCount,
            int teamSize,
            DraftOrderStrategy strategy,
            List<String> playerNames
    ) {
        Template template = new Template(name, TemplateConfiguration.draft(teamCount, teamSize, strategy));
        template.registerPlayers(playerNames).requireValidRoster(template.players());
        return template;
    }

    public static Template reference(TemplateId templateId) {
        return new Template(templateId);
    }

    private Template registerPlayers(List<String> playerNames) {
        persistentPlayers.clear();
        for (int index = 0; index < playerNames.size(); index++) {
            String playerName = playerNames.get(index);
            TemplatePlayer player = new TemplatePlayer(id, playerName, index);
            player.attach(this);
            persistentPlayers.add(player);
        }
        return this;
    }
}
