package com.naminhyeok.fantazzk.room;

import com.naminhyeok.fantazzk.room.domain.game.*;
import com.naminhyeok.fantazzk.room.domain.handoff.*;
import com.naminhyeok.fantazzk.room.domain.repository.*;
import com.naminhyeok.fantazzk.room.domain.room.*;
import com.naminhyeok.fantazzk.room.domain.shared.*;

import static org.assertj.core.api.Assertions.assertThat;

import com.naminhyeok.fantazzk.room.domain.handoff.GameRules;
import com.naminhyeok.fantazzk.room.domain.handoff.StartedAuctionParticipant;
import com.naminhyeok.fantazzk.room.domain.handoff.StartedDraftParticipant;
import com.naminhyeok.fantazzk.room.domain.handoff.StartedGamePlayer;
import com.naminhyeok.fantazzk.room.domain.handoff.StartedGameSnapshot;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class GameFactoryTest {
    private static final Instant STARTED_AT = Instant.parse("2026-04-15T00:00:00Z");

    private final GameFactory gameFactory = new GameFactory();

    @Test
    void 경매전_시작_스냅샷으로_경매_게임을_생성한다() {
        StartedGameSnapshot snapshot =
            new StartedGameSnapshot(
                new RoomId(UUID.fromString("00000000-0000-0000-0000-000000000011")),
                "ROOM01",
                new GameId(UUID.fromString("00000000-0000-0000-0000-000000000101")),
                STARTED_AT,
                GameRules.auction(2, 2, 300, 45, 10, 1),
                List.of(
                    new StartedAuctionParticipant(new TeamLeaderId("host-1"), "호스트", 300),
                    new StartedAuctionParticipant(new TeamLeaderId("guest-1"), "게스트", 300)
                ),
                List.of(
                    new StartedGamePlayer(new RoomPlayerId(0), "선수1", "TOP", 0),
                    new StartedGamePlayer(new RoomPlayerId(1), "선수2", "JUNGLE", 1)
                )
            );

        Game created = gameFactory.create(snapshot);

        assertThat(created).isInstanceOf(AuctionGame.class);
        assertThat(created.getId()).isEqualTo(snapshot.gameId());
        assertThat(created.getRoomId()).isEqualTo(snapshot.roomId());
        assertThat(created.getRoomCode()).isEqualTo(snapshot.roomCode());
        assertThat(created.getStatus()).isEqualTo(GameStatus.IN_PROGRESS);
        assertThat(created.getStartedAt()).isEqualTo(snapshot.startedAt());
        assertThat(created.getRules()).isEqualTo(snapshot.rules());
        assertThat(created.getParticipants())
            .extracting(GameParticipant::teamLeaderId, GameParticipant::nickname, GameParticipant::remainingBudget)
            .containsExactly(
                org.assertj.core.groups.Tuple.tuple(new TeamLeaderId("host-1"), "호스트", 300),
                org.assertj.core.groups.Tuple.tuple(new TeamLeaderId("guest-1"), "게스트", 300)
            );
        assertThat(created.getPlayerPool())
            .extracting(GamePlayer::playerId, GamePlayer::name, GamePlayer::position, GamePlayer::displayOrder)
            .containsExactly(
                org.assertj.core.groups.Tuple.tuple(new RoomPlayerId(0), "선수1", "TOP", 0),
                org.assertj.core.groups.Tuple.tuple(new RoomPlayerId(1), "선수2", "JUNGLE", 1)
            );
        AuctionGame auctionGame = (AuctionGame) created;
        assertThat(auctionGame.getCurrentRound()).isEqualTo(1);
        assertThat(auctionGame.getCurrentRoundEndsAt()).isEqualTo(STARTED_AT.plusSeconds(45));
    }

    @Test
    void 드래프트전_시작_스냅샷으로_드래프트_게임을_생성한다() {
        StartedGameSnapshot snapshot =
            new StartedGameSnapshot(
                new RoomId(UUID.fromString("00000000-0000-0000-0000-000000000012")),
                "ROOM02",
                new GameId(UUID.fromString("00000000-0000-0000-0000-000000000102")),
                STARTED_AT,
                GameRules.draft(2, 2, 30, DraftOrderStrategy.SNAKE),
                List.of(
                    new StartedDraftParticipant(new TeamLeaderId("host-1"), "호스트", 1),
                    new StartedDraftParticipant(new TeamLeaderId("guest-1"), "게스트", 2)
                ),
                List.of(
                    new StartedGamePlayer(new RoomPlayerId(0), "선수1", "TOP", 0),
                    new StartedGamePlayer(new RoomPlayerId(1), "선수2", "JUNGLE", 1)
                )
            );

        Game created = gameFactory.create(snapshot);

        assertThat(created).isInstanceOf(DraftGame.class);
        assertThat(created.getId()).isEqualTo(snapshot.gameId());
        assertThat(created.getRoomId()).isEqualTo(snapshot.roomId());
        assertThat(created.getRoomCode()).isEqualTo(snapshot.roomCode());
        assertThat(created.getStatus()).isEqualTo(GameStatus.IN_PROGRESS);
        assertThat(created.getStartedAt()).isEqualTo(snapshot.startedAt());
        assertThat(created.getRules()).isEqualTo(snapshot.rules());
        assertThat(created.getParticipants())
            .extracting(GameParticipant::teamLeaderId, GameParticipant::nickname, GameParticipant::draftPosition)
            .containsExactly(
                org.assertj.core.groups.Tuple.tuple(new TeamLeaderId("host-1"), "호스트", 1),
                org.assertj.core.groups.Tuple.tuple(new TeamLeaderId("guest-1"), "게스트", 2)
            );
        assertThat(created.getPlayerPool())
            .extracting(GamePlayer::playerId, GamePlayer::name, GamePlayer::position, GamePlayer::displayOrder)
            .containsExactly(
                org.assertj.core.groups.Tuple.tuple(new RoomPlayerId(0), "선수1", "TOP", 0),
                org.assertj.core.groups.Tuple.tuple(new RoomPlayerId(1), "선수2", "JUNGLE", 1)
            );
        DraftGame draftGame = (DraftGame) created;
        assertThat(draftGame.getCurrentTurnIndex()).isEqualTo(0);
    }

    @Test
    void 시작_스냅샷은_규칙과_참가자_모드가_다르면_생성_시점에_거부한다() {
        assertThatThrownBy(
            () -> new StartedGameSnapshot(
                new RoomId(UUID.fromString("00000000-0000-0000-0000-000000000013")),
                "ROOM03",
                new GameId(UUID.fromString("00000000-0000-0000-0000-000000000103")),
                STARTED_AT,
                GameRules.auction(2, 2, 300, 45, 10, 1),
                List.of(
                    new StartedDraftParticipant(new TeamLeaderId("host-1"), "호스트", 1),
                    new StartedDraftParticipant(new TeamLeaderId("guest-1"), "게스트", 2)
                ),
                List.of(
                    new StartedGamePlayer(new RoomPlayerId(0), "선수1", "TOP", 0),
                    new StartedGamePlayer(new RoomPlayerId(1), "선수2", "JUNGLE", 1)
                )
            )
        )
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("handoff participant");
    }
}
