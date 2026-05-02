package com.naminhyeok.fantazzk.room.domain;

import java.util.List;
import java.util.Objects;

public record RoomTemplateSpec(
    String gameType,
    RoomMode mode,
    int teamCount,
    int teamSize,
    Integer budget,
    int pickBanTime,
    Integer minBidUnit,
    DraftOrderStrategy draftOrderStrategy,
    List<Player> players
) {
    public RoomTemplateSpec {
        Objects.requireNonNull(mode, "mode must not be null");
        if (teamCount <= 0) {
            throw new IllegalArgumentException("팀 수는 0보다 커야 합니다");
        }
        if (teamSize <= 0) {
            throw new IllegalArgumentException("팀 크기는 0보다 커야 합니다");
        }
        if (pickBanTime <= 0) {
            throw new IllegalArgumentException("픽밴 시간은 0보다 커야 합니다");
        }
        players = List.copyOf(players);

        if (mode == RoomMode.AUCTION) {
            if (budget == null) {
                throw new IllegalArgumentException("경매 방 생성 명세에는 예산이 필요합니다");
            }
            if (minBidUnit == null) {
                throw new IllegalArgumentException("경매 방 생성 명세에는 최소 입찰 단위가 필요합니다");
            }
            if (draftOrderStrategy != null) {
                throw new IllegalArgumentException("경매 방 생성 명세에는 드래프트 순서 전략을 지정할 수 없습니다");
            }
        }

        if (mode == RoomMode.DRAFT) {
            if (budget != null) {
                throw new IllegalArgumentException("드래프트 방 생성 명세에는 예산을 지정할 수 없습니다");
            }
            if (minBidUnit != null) {
                throw new IllegalArgumentException("드래프트 방 생성 명세에는 최소 입찰 단위를 지정할 수 없습니다");
            }
            if (draftOrderStrategy == null) {
                throw new IllegalArgumentException("드래프트 방 생성 명세에는 순서 전략이 필요합니다");
            }
        }
    }

    public record Player(RoomPlayerId id, String name, String position, int displayOrder) {
    }
}
