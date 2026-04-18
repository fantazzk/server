package com.naminhyeok.fantazzk.room;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;

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
                RoomMode.AUCTION,
                GameRules.auction(2, 2, 300, 45, 10, 1),
                List.of(
                    GameParticipant.auction(new TeamLeaderId("host-1"), "호스트", 300),
                    GameParticipant.auction(new TeamLeaderId("guest-1"), "게스트", 300)
                ),
                List.of(
                    new GamePlayer(new RoomPlayerId(0), "선수1", "TOP", 0),
                    new GamePlayer(new RoomPlayerId(1), "선수2", "JUNGLE", 1)
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
        assertThat(created.getParticipants()).containsExactlyElementsOf(snapshot.participants());
        assertThat(created.getPlayerPool()).containsExactlyElementsOf(snapshot.playerPool());
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
                RoomMode.DRAFT,
                GameRules.draft(2, 2, 30, DraftOrderStrategy.SNAKE),
                List.of(
                    GameParticipant.draft(new TeamLeaderId("host-1"), "호스트", 1),
                    GameParticipant.draft(new TeamLeaderId("guest-1"), "게스트", 2)
                ),
                List.of(
                    new GamePlayer(new RoomPlayerId(0), "선수1", "TOP", 0),
                    new GamePlayer(new RoomPlayerId(1), "선수2", "JUNGLE", 1)
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
        assertThat(created.getParticipants()).containsExactlyElementsOf(snapshot.participants());
        assertThat(created.getPlayerPool()).containsExactlyElementsOf(snapshot.playerPool());
        DraftGame draftGame = (DraftGame) created;
        assertThat(draftGame.getCurrentTurnIndex()).isEqualTo(0);
    }
}
