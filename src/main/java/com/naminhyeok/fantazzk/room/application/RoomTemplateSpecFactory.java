package com.naminhyeok.fantazzk.room.application;

import com.naminhyeok.fantazzk.room.domain.DraftOrderStrategy;
import com.naminhyeok.fantazzk.room.domain.RoomMode;
import com.naminhyeok.fantazzk.room.domain.RoomPlayerId;
import com.naminhyeok.fantazzk.room.domain.RoomTemplateSpec;
import com.naminhyeok.fantazzk.template.TemplateCatalog;
import java.util.List;
import java.util.Objects;

final class RoomTemplateSpecFactory {
    private RoomTemplateSpecFactory() {
    }

    static RoomTemplateSpec from(TemplateCatalog.TemplateBlueprint template) {
        Objects.requireNonNull(template, "template must not be null");
        List<RoomTemplateSpec.Player> players = template.players().stream()
            .map(RoomTemplateSpecFactory::playerFrom)
            .toList();
        int requiredPlayerCount = template.teamCount() * (template.teamSize() - 1);
        if (players.size() != requiredPlayerCount) {
            throw new IllegalArgumentException("선수 수는 정확히 " + requiredPlayerCount + "명이어야 합니다");
        }
        return new RoomTemplateSpec(
            template.gameType(),
            roomModeFrom(template.mode()),
            template.teamCount(),
            template.teamSize(),
            template.budget(),
            requirePickBanTime(template.pickBanTime()),
            template.minBidUnit(),
            draftOrderStrategyFrom(template.draftOrderStrategy()),
            players
        );
    }

    private static RoomMode roomModeFrom(TemplateCatalog.Mode mode) {
        return switch (Objects.requireNonNull(mode, "mode must not be null")) {
            case AUCTION -> RoomMode.AUCTION;
            case DRAFT -> RoomMode.DRAFT;
        };
    }

    private static DraftOrderStrategy draftOrderStrategyFrom(TemplateCatalog.DraftOrderStrategy strategy) {
        if (strategy == null) {
            return null;
        }
        return switch (strategy) {
            case SNAKE -> DraftOrderStrategy.SNAKE;
            case FIXED -> DraftOrderStrategy.FIXED;
        };
    }

    private static RoomTemplateSpec.Player playerFrom(TemplateCatalog.PlayerBlueprint player) {
        Objects.requireNonNull(player, "player must not be null");
        return new RoomTemplateSpec.Player(
            new RoomPlayerId(player.playerIndex()),
            player.name(),
            player.position(),
            player.playerIndex()
        );
    }

    private static int requirePickBanTime(Integer pickBanTime) {
        if (pickBanTime == null) {
            throw new IllegalArgumentException("방 생성 명세에는 픽밴 시간이 필요합니다");
        }
        return pickBanTime;
    }
}
