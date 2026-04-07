package com.naminhyeok.fantazzk.template;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import lombok.Getter;
import org.jmolecules.ddd.types.AggregateRoot;
import org.jmolecules.ddd.types.Identifier;

@Getter
public class Template implements AggregateRoot<Template, Template.TemplateId> {
    private final TemplateId id;
    private String name;
    private TemplateConfiguration configuration;
    private List<TemplatePlayer> players;

    Template(String name, TemplateConfiguration configuration) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("템플릿 이름은 비어 있을 수 없습니다");
        }

        this.id = new TemplateId(UUID.randomUUID());
        this.name = name;
        this.configuration = configuration;
        this.players = new ArrayList<>();
    }

    public static Template createAuction(
        String name,
        int teamCount,
        int teamSize,
        int budget,
        List<String> playerNames
    ) {
        return new Template(name, TemplateConfiguration.auction(teamCount, teamSize, budget))
            .registerPlayers(playerNames);
    }

    public static Template createDraft(
        String name,
        int teamCount,
        int teamSize,
        DraftOrderStrategy strategy,
        List<String> playerNames
    ) {
        return new Template(name, TemplateConfiguration.draft(teamCount, teamSize, strategy))
            .registerPlayers(playerNames);
    }

    public TemplateId getId() {
        return id;
    }

    public TemplateMode getMode() {
        return configuration.getMode();
    }

    public int getTeamCount() {
        return configuration.getTeamCount();
    }

    public int getTeamSize() {
        return configuration.getTeamSize();
    }

    public Integer getBudget() {
        return configuration.getBudget();
    }

    public DraftOrderStrategy getDraftOrderStrategy() {
        return configuration.getDraftOrderStrategy();
    }

    public List<TemplatePlayer> getPlayers() {
        return players.stream()
            .sorted(Comparator.comparingInt(TemplatePlayer::getDisplayOrder))
            .toList();
    }

    public int getPicksPerTeam() {
        return configuration.getTeamSize() - 1;
    }

    private Template registerPlayers(List<String> playerNames) {
        if (playerNames.size() != configuration.requiredPlayerCount()) {
            throw new IllegalArgumentException("선수 수는 정확히 " + configuration.requiredPlayerCount() + "명이어야 합니다");
        }

        players.clear();
        for (int index = 0; index < playerNames.size(); index++) {
            players.add(new TemplatePlayer(playerNames.get(index), index));
        }

        return this;
    }

    public record TemplateId(UUID templateId) implements Identifier {
    }
}
