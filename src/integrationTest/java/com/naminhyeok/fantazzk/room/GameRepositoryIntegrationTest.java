package com.naminhyeok.fantazzk.room;

import static org.assertj.core.api.Assertions.assertThat;

import com.naminhyeok.fantazzk.room.domain.AuctionGame;
import com.naminhyeok.fantazzk.room.domain.DraftGame;
import com.naminhyeok.fantazzk.room.domain.DraftOrderStrategy;
import com.naminhyeok.fantazzk.room.domain.Game;
import com.naminhyeok.fantazzk.room.domain.GameFactory;
import com.naminhyeok.fantazzk.room.domain.GameId;
import com.naminhyeok.fantazzk.room.domain.GameParticipant;
import com.naminhyeok.fantazzk.room.domain.GamePlayer;
import com.naminhyeok.fantazzk.room.domain.GameRules;
import com.naminhyeok.fantazzk.room.domain.GameStatus;
import com.naminhyeok.fantazzk.room.domain.Room;
import com.naminhyeok.fantazzk.room.domain.RoomMode;
import com.naminhyeok.fantazzk.room.domain.RoomPlayerId;
import com.naminhyeok.fantazzk.room.domain.RoomTemplateSpec;
import com.naminhyeok.fantazzk.room.domain.RosterMember;
import com.naminhyeok.fantazzk.room.domain.StartedGameSnapshot;
import com.naminhyeok.fantazzk.room.domain.TeamLeaderId;
import com.naminhyeok.fantazzk.room.repository.Games;
import com.naminhyeok.fantazzk.room.repository.Rooms;
import jakarta.persistence.EntityManager;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.test.context.TestConstructor;
import org.springframework.transaction.annotation.Transactional;

@DataJpaTest(
    properties = {
        "spring.datasource.url=jdbc:h2:mem:game-repository-test;DB_CLOSE_DELAY=-1",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.datasource.username=sa",
        "spring.datasource.password=",
        "spring.jpa.hibernate.ddl-auto=validate",
        "spring.liquibase.enabled=true",
        "sentry.enabled=false"
    }
)
@TestConstructor(autowireMode = TestConstructor.AutowireMode.ALL)
@RequiredArgsConstructor
class GameRepositoryIntegrationTest {
    private static final Instant STARTED_AT = Instant.parse("2026-04-15T00:00:00Z");

    private final Rooms rooms;
    private final Games games;
    private final EntityManager entityManager;

    @Test
    @Transactional
    void 경매_게임_기본_상태를_저장하고_다시_읽는다() {
        Room room = auctionRoom("ROOM01", Instant.parse("2026-04-14T23:59:00Z"));
        rooms.save(room);

        Game saved =
            games.save(
                new GameFactory().create(
                    new StartedGameSnapshot(
                        room.getId(),
                        room.getCode(),
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
                    )
                )
            );

        entityManager.flush();
        entityManager.clear();

        Game reloaded = games.findById(saved.getId()).orElseThrow();

        assertThat(reloaded).isInstanceOf(AuctionGame.class);
        assertThat(reloaded.getId()).isEqualTo(saved.getId());
        assertThat(reloaded.getRoomId()).isEqualTo(room.getId());
        assertThat(reloaded.getRoomCode()).isEqualTo("ROOM01");
        assertThat(reloaded.getStatus()).isEqualTo(GameStatus.IN_PROGRESS);
        assertThat(reloaded.getStartedAt()).isEqualTo(STARTED_AT);
        assertThat(reloaded.getRules()).isEqualTo(GameRules.auction(2, 2, 300, 45, 10, 1));
        assertThat(reloaded.getParticipants())
            .containsExactly(
                GameParticipant.auction(new TeamLeaderId("host-1"), "호스트", 300),
                GameParticipant.auction(new TeamLeaderId("guest-1"), "게스트", 300)
            );
        assertThat(reloaded.getPlayerPool())
            .containsExactly(
                new GamePlayer(new RoomPlayerId(0), "선수1", "TOP", 0),
                new GamePlayer(new RoomPlayerId(1), "선수2", "JUNGLE", 1)
            );
        AuctionGame auctionGame = (AuctionGame) reloaded;
        assertThat(auctionGame.getCurrentRound()).isEqualTo(1);
        assertThat(auctionGame.getCurrentRoundEndsAt()).isEqualTo(STARTED_AT.plusSeconds(45));
    }

    @Test
    @Transactional
    void 경매_live_state를_변경하면_game_version이_증가한다() {
        Room room = auctionRoom("ROOM03", Instant.parse("2026-04-14T23:59:00Z"));
        room.join(new TeamLeaderId("guest-1"), "게스트", "guest-action-token");
        rooms.save(room);

        AuctionGame saved =
            (AuctionGame) games.save(
                new GameFactory().create(
                    new StartedGameSnapshot(
                        room.getId(),
                        room.getCode(),
                        new GameId(UUID.fromString("00000000-0000-0000-0000-000000000103")),
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
                    )
                )
            );
        entityManager.flush();
        entityManager.clear();

        AuctionGame reloaded = (AuctionGame) games.findById(saved.getId()).orElseThrow();
        long initialVersion = reloaded.getVersion();

        reloaded.placeBid(new TeamLeaderId("host-1"), 100, STARTED_AT.plusSeconds(1));
        entityManager.flush();
        long versionAfterBid = reloaded.getVersion();

        reloaded.settleAuction(STARTED_AT.plusSeconds(46));
        entityManager.flush();
        long versionAfterSettlement = reloaded.getVersion();

        assertThat(versionAfterBid).isGreaterThan(initialVersion);
        assertThat(versionAfterSettlement).isGreaterThan(versionAfterBid);
    }

    @Test
    @Transactional
    void 드래프트_게임_기본_상태를_저장하고_다시_읽는다() {
        Room room = draftRoom("ROOM02", Instant.parse("2026-04-14T23:59:00Z"));
        rooms.save(room);

        Game saved =
            games.save(
                new GameFactory().create(
                    new StartedGameSnapshot(
                        room.getId(),
                        room.getCode(),
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
                    )
                )
            );

        entityManager.flush();
        entityManager.clear();

        Game reloaded = games.findById(saved.getId()).orElseThrow();

        assertThat(reloaded).isInstanceOf(DraftGame.class);
        assertThat(reloaded.getId()).isEqualTo(saved.getId());
        assertThat(reloaded.getRoomId()).isEqualTo(room.getId());
        assertThat(reloaded.getRoomCode()).isEqualTo("ROOM02");
        assertThat(reloaded.getStatus()).isEqualTo(GameStatus.IN_PROGRESS);
        assertThat(reloaded.getStartedAt()).isEqualTo(STARTED_AT);
        assertThat(reloaded.getRules()).isEqualTo(GameRules.draft(2, 2, 30, DraftOrderStrategy.SNAKE));
        assertThat(reloaded.getParticipants())
            .containsExactly(
                GameParticipant.draft(new TeamLeaderId("host-1"), "호스트", 1),
                GameParticipant.draft(new TeamLeaderId("guest-1"), "게스트", 2)
            );
        assertThat(reloaded.getPlayerPool())
            .containsExactly(
                new GamePlayer(new RoomPlayerId(0), "선수1", "TOP", 0),
                new GamePlayer(new RoomPlayerId(1), "선수2", "JUNGLE", 1)
            );
        DraftGame draftGame = (DraftGame) reloaded;
        assertThat(draftGame.getCurrentTurnIndex()).isEqualTo(0);
    }

    @Test
    @Transactional
    void 드래프트_live_state_멤버를_저장하고_다시_읽는다() {
        Room room = draftRoom("ROOM04", Instant.parse("2026-04-14T23:59:00Z"));
        rooms.save(room);

        DraftGame saved =
            (DraftGame) games.save(
                new GameFactory().create(
                    new StartedGameSnapshot(
                        room.getId(),
                        room.getCode(),
                        new GameId(UUID.fromString("00000000-0000-0000-0000-000000000104")),
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
                    )
                )
            );

        saved.pick(new TeamLeaderId("host-1"), "선수1");
        entityManager.flush();
        entityManager.clear();

        DraftGame reloaded = (DraftGame) games.findById(saved.getId()).orElseThrow();

        assertThat(reloaded.getMembers())
            .containsExactly(new RosterMember(new TeamLeaderId("host-1"), "선수1", 0));
        assertThat(reloaded.getCurrentTurnIndex()).isEqualTo(1);
        assertThat(reloaded.isPlayerAvailable("선수1")).isFalse();
        assertThat(reloaded.isPlayerAvailable("선수2")).isTrue();
    }

    private static Room auctionRoom(String code, Instant createdAt) {
        return Room.createFromTemplate(
            code,
            new TeamLeaderId("host-1"),
            "호스트",
            "host-action-token",
            new RoomTemplateSpec(
                RoomMode.AUCTION,
                2,
                2,
                300,
                45,
                10,
                1,
                null,
                List.of(
                    new RoomTemplateSpec.Player(new RoomPlayerId(0), "선수1", "TOP", 0),
                    new RoomTemplateSpec.Player(new RoomPlayerId(1), "선수2", "JUNGLE", 1)
                )
            ),
            createdAt
        );
    }

    private static Room draftRoom(String code, Instant createdAt) {
        return Room.createFromTemplate(
            code,
            new TeamLeaderId("host-1"),
            "호스트",
            "host-action-token",
            new RoomTemplateSpec(
                RoomMode.DRAFT,
                2,
                2,
                null,
                30,
                null,
                DraftOrderStrategy.SNAKE,
                List.of(
                    new RoomTemplateSpec.Player(new RoomPlayerId(0), "선수1", "TOP", 0),
                    new RoomTemplateSpec.Player(new RoomPlayerId(1), "선수2", "JUNGLE", 1)
                )
            ),
            createdAt
        );
    }
}
