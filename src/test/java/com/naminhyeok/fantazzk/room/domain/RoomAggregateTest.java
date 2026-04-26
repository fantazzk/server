package com.naminhyeok.fantazzk.room.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.naminhyeok.fantazzk.CoreException;
import java.nio.charset.StandardCharsets;
import com.naminhyeok.fantazzk.room.domain.DraftOrderStrategy;
import com.naminhyeok.fantazzk.room.domain.GameId;
import com.naminhyeok.fantazzk.room.domain.GameParticipant;
import com.naminhyeok.fantazzk.room.domain.GamePlayer;
import com.naminhyeok.fantazzk.room.domain.GameRules;
import com.naminhyeok.fantazzk.room.domain.Room;
import com.naminhyeok.fantazzk.room.domain.RoomErrorType;
import com.naminhyeok.fantazzk.room.domain.RoomMode;
import com.naminhyeok.fantazzk.room.domain.RoomPlayer;
import com.naminhyeok.fantazzk.room.domain.RoomPlayerId;
import com.naminhyeok.fantazzk.room.domain.RoomStartReadiness;
import com.naminhyeok.fantazzk.room.domain.RoomStatus;
import com.naminhyeok.fantazzk.room.domain.RoomTeamLeader;
import com.naminhyeok.fantazzk.room.domain.RoomTemplateSpec;
import com.naminhyeok.fantazzk.room.domain.StartedGameSnapshot;
import com.naminhyeok.fantazzk.room.domain.TeamLeaderId;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class RoomAggregateTest {
    private static final Instant CREATED_AT = Instant.parse("2026-04-09T00:00:00Z");
    private static final Instant STARTED_AT = Instant.parse("2026-04-09T00:10:00Z");
    private static final String HOST_ID = "host-1";
    private static final String HOST_ACTION_TOKEN = "host-action-token";
    private static final String GUEST_ID = "guest-1";
    private static final String GUEST_ACTION_TOKEN = "guest-action-token";

    @Test
    void 템플릿_명세로_방을_생성하면_선수와_호스트를_초기화한다() {
        Room room =
            Room.createFromTemplate(
                "ROOM01",
                new TeamLeaderId(HOST_ID),
                "  호스트  ",
                HOST_ACTION_TOKEN,
                new RoomTemplateSpec(
                    "LEAGUE_OF_LEGENDS",
                    RoomMode.AUCTION,
                    2,
                    3,
                    300,
                    45,
                    10,
                    null,
                    List.of(
                        new RoomTemplateSpec.Player(new RoomPlayerId(1), "선수B", "JUNGLE", 1),
                        new RoomTemplateSpec.Player(new RoomPlayerId(0), "선수A", "TOP", 0)
                    )
                ),
                CREATED_AT
            );

        assertThat(room.getCode()).isEqualTo("ROOM01");
        assertThat(room.getCreatedAt()).isEqualTo(CREATED_AT);
        assertThat(room.getStatus()).isEqualTo(RoomStatus.WAITING);
        assertThat(room.getMode()).isEqualTo(RoomMode.AUCTION);
        assertThat(room.getPickBanTime()).isEqualTo(45);
        assertThat(room.getPlayers().stream().map(RoomPlayer::getName)).containsExactly("선수A", "선수B");
        assertThat(room.getPlayers().stream().map(RoomPlayer::getPosition)).containsExactly("TOP", "JUNGLE");
        assertThat(room.getPlayers().stream().map(RoomPlayer::getId))
            .containsExactly(new RoomPlayerId(0), new RoomPlayerId(1));

        RoomTeamLeader hostLeader = room.getLeaders().getFirst();
        assertThat(hostLeader.getNickname()).isEqualTo("호스트");
        assertThat(hostLeader.getRemainingBudget()).isEqualTo(300);
        assertThat(hostLeader.getActionToken()).isEqualTo(HOST_ACTION_TOKEN);
        assertThat(hostLeader.getId()).isEqualTo(new TeamLeaderId(HOST_ID));
    }

    @Test
    void 참여_가능한_대기룸만_joinable이다() {
        Room joinable = auctionWaitingRoom(CREATED_AT);

        Room full = auctionWaitingRoom(CREATED_AT.plusSeconds(60));
        full.join(new TeamLeaderId(GUEST_ID), "게스트", GUEST_ACTION_TOKEN);

        Room started = auctionWaitingRoom(CREATED_AT.plusSeconds(120));
        started.join(new TeamLeaderId(GUEST_ID), "게스트", GUEST_ACTION_TOKEN);
        started.start(new TeamLeaderId(HOST_ID), deterministicGameId(started), STARTED_AT);

        assertThat(joinable.isJoinable()).isTrue();
        assertThat(full.isJoinable()).isFalse();
        assertThat(started.isJoinable()).isFalse();
    }

    @Test
    void 참가하면_팀장을_추가한다() {
        Room room = auctionWaitingRoom();

        RoomTeamLeader joinedLeader = room.join(new TeamLeaderId(GUEST_ID), "  게스트  ", GUEST_ACTION_TOKEN);

        assertThat(room.getLeaders()).hasSize(2);
        assertThat(joinedLeader.getId()).isEqualTo(new TeamLeaderId(GUEST_ID));
        assertThat(joinedLeader.getNickname()).isEqualTo("게스트");
        assertThat(joinedLeader.getActionToken()).isEqualTo(GUEST_ACTION_TOKEN);
        assertThat(room.getLeaders().getLast()).isSameAs(joinedLeader);
    }

    @Test
    void 참가자는_공백과_대소문자를_무시하고_중복된_닉네임으로_참가할_수_없다() {
        Room room =
            Room.createFromTemplate(
                "ROOM01",
                new TeamLeaderId(HOST_ID),
                "Faker",
                HOST_ACTION_TOKEN,
                new RoomTemplateSpec(
                    "LEAGUE_OF_LEGENDS",
                    RoomMode.AUCTION,
                    2,
                    2,
                    300,
                    45,
                    10,
                    null,
                    List.of(
                        new RoomTemplateSpec.Player(new RoomPlayerId(0), "선수1", "TOP", 0),
                        new RoomTemplateSpec.Player(new RoomPlayerId(1), "선수2", "JUNGLE", 1)
                    )
                ),
                CREATED_AT
            );

        assertThatThrownBy(() -> room.join(new TeamLeaderId(GUEST_ID), "  faker  ", GUEST_ACTION_TOKEN))
            .isInstanceOf(CoreException.class)
            .isInstanceOfSatisfying(CoreException.class, ex -> {
                assertThat(ex.getError()).isEqualTo(RoomErrorType.ROOM_NICKNAME_ALREADY_TAKEN);
                assertThat(ex.getData()).isNull();
            });
    }

    @Test
    void 드래프트_자리를_선택하면_팀장에게_확정된다() {
        Room room = waitingDraftRoom();
        room.join(new TeamLeaderId(GUEST_ID), "게스트", GUEST_ACTION_TOKEN);

        room.selectDraftPosition(new TeamLeaderId(HOST_ID), 2);

        assertThat(room.getLeaders().getFirst().getDraftPosition()).isEqualTo(2);
        assertThat(room.getStartReadiness()).isEqualTo(RoomStartReadiness.WAITING_FOR_DRAFT_POSITIONS);
    }

    @Test
    void 드래프트_자리를_다른_빈_자리로_변경할_수_있다() {
        Room room = waitingDraftRoom();
        room.join(new TeamLeaderId(GUEST_ID), "게스트", GUEST_ACTION_TOKEN);
        room.selectDraftPosition(new TeamLeaderId(HOST_ID), 1);

        room.selectDraftPosition(new TeamLeaderId(HOST_ID), 2);

        assertThat(room.getLeaders().getFirst().getDraftPosition()).isEqualTo(2);
    }

    @Test
    void 드래프트_자리를_취소하면_미선택으로_돌아간다() {
        Room room = waitingDraftRoom();
        room.join(new TeamLeaderId(GUEST_ID), "게스트", GUEST_ACTION_TOKEN);
        room.selectDraftPosition(new TeamLeaderId(HOST_ID), 1);

        room.clearDraftPosition(new TeamLeaderId(HOST_ID));

        assertThat(room.getLeaders().getFirst().getDraftPosition()).isNull();
    }

    @Test
    void 이미_선점된_드래프트_자리는_선택할_수_없다() {
        Room room = waitingDraftRoom();
        room.join(new TeamLeaderId(GUEST_ID), "게스트", GUEST_ACTION_TOKEN);
        room.selectDraftPosition(new TeamLeaderId(HOST_ID), 1);

        assertThatThrownBy(() -> room.selectDraftPosition(new TeamLeaderId(GUEST_ID), 1))
            .isInstanceOf(CoreException.class)
            .satisfies(ex -> {
                CoreException coreException = (CoreException) ex;
                assertThat(coreException.getError()).isEqualTo(RoomErrorType.ROOM_DRAFT_POSITION_TAKEN);
                assertThat(coreException.getData()).isNull();
            });
    }

    @Test
    void 대기_상태가_아니면_참가할_수_없다() {
        Room room = startedAuctionRoom();

        assertThatThrownBy(() -> room.join(new TeamLeaderId("guest-2"), "추가 게스트", "guest-2-action-token"))
            .isInstanceOf(CoreException.class)
            .satisfies(ex -> {
                CoreException coreException = (CoreException) ex;
                assertThat(coreException.getError()).isEqualTo(RoomErrorType.ROOM_JOIN_REQUIRES_WAITING);
                assertThat(coreException.getData()).isNull();
            });
    }

    @Test
    void 방이_가득_차면_참가할_수_없다() {
        Room room =
            Room.createFromTemplate(
                "ROOM03",
                new TeamLeaderId(HOST_ID),
                "호스트",
                HOST_ACTION_TOKEN,
                new RoomTemplateSpec(
                    "LEAGUE_OF_LEGENDS",
                    RoomMode.AUCTION,
                    1,
                    2,
                    300,
                    45,
                    10,
                    null,
                    List.of(
                        new RoomTemplateSpec.Player(new RoomPlayerId(0), "선수1", "TOP", 0),
                        new RoomTemplateSpec.Player(new RoomPlayerId(1), "선수2", "JUNGLE", 1)
                    )
                ),
                CREATED_AT
        );

        assertThatThrownBy(() -> room.join(new TeamLeaderId(GUEST_ID), "게스트", GUEST_ACTION_TOKEN))
            .isInstanceOf(CoreException.class)
            .satisfies(ex -> {
                CoreException coreException = (CoreException) ex;
                assertThat(coreException.getError()).isEqualTo(RoomErrorType.ROOM_FULL);
                assertThat(coreException.getData()).isNull();
            });
    }

    @Nested
    class 시작 {
        @Test
        void 방을_시작하면_시작_스냅샷을_반환하고_시작_정보를_기록한다() {
            Room room = auctionWaitingRoom();
            room.join(new TeamLeaderId(GUEST_ID), "게스트", GUEST_ACTION_TOKEN);

            GameId gameId = new GameId(UUID.fromString("00000000-0000-0000-0000-000000000123"));
            StartedGameSnapshot snapshot = room.start(new TeamLeaderId(HOST_ID), gameId, STARTED_AT);

            assertThat(snapshot.roomId()).isEqualTo(room.getId());
            assertThat(snapshot.roomCode()).isEqualTo(room.getCode());
            assertThat(snapshot.gameId()).isEqualTo(gameId);
            assertThat(snapshot.startedAt()).isEqualTo(STARTED_AT);
            assertThat(snapshot.gameType()).isEqualTo("LEAGUE_OF_LEGENDS");
            assertThat(snapshot.gameMode()).isEqualTo(room.getMode());
            assertThat(snapshot.rules())
                .isEqualTo(
                    GameRules.auction(
                        room.getTeamCount(),
                        room.getTeamSize(),
                        room.getBudget(),
                        room.getPickBanTime(),
                        room.getMinBidUnit()
                    )
                );
            assertThat(snapshot.participants())
                .extracting(GameParticipant::teamLeaderId, GameParticipant::nickname, GameParticipant::remainingBudget)
                .containsExactly(
                    org.assertj.core.groups.Tuple.tuple(new TeamLeaderId(HOST_ID), "호스트", 300),
                    org.assertj.core.groups.Tuple.tuple(new TeamLeaderId(GUEST_ID), "게스트", 300)
                );
            assertThat(snapshot.playerPool())
                .containsExactly(
                    new GamePlayer(new RoomPlayerId(0), "선수1", "TOP", 0),
                    new GamePlayer(new RoomPlayerId(1), "선수2", "JUNGLE", 1)
                );
            assertThat(room.getStartedGameId()).isEqualTo(gameId);
            assertThat(room.getStartedAt()).isEqualTo(STARTED_AT);
        }

        @Test
        void 경매_방을_시작하면_started_상태로_전환한다() {
            Room room = auctionWaitingRoom();
            room.join(new TeamLeaderId(GUEST_ID), "게스트", GUEST_ACTION_TOKEN);

            room.start(new TeamLeaderId(HOST_ID), deterministicGameId(room), STARTED_AT);

            assertThat(room.getStatus()).isEqualTo(RoomStatus.STARTED);
            assertThat(room.getStartedGameId()).isNotNull();
            assertThat(room.getStartedAt()).isNotNull();
        }

        @Test
        void 드래프트_방을_시작하면_started_상태로_전환한다() {
            Room room = waitingDraftRoom();
            room.join(new TeamLeaderId(GUEST_ID), "게스트", GUEST_ACTION_TOKEN);
            room.selectDraftPosition(new TeamLeaderId(HOST_ID), 1);
            room.selectDraftPosition(new TeamLeaderId(GUEST_ID), 2);

            room.start(new TeamLeaderId(HOST_ID), deterministicGameId(room), STARTED_AT);

            assertThat(room.getStatus()).isEqualTo(RoomStatus.STARTED);
            assertThat(room.getStartedGameId()).isNotNull();
            assertThat(room.getStartedAt()).isNotNull();
        }

        @Test
        void 드래프트_방은_자리_확정이_끝나야_시작할_수_있다() {
            Room room = waitingDraftRoom();
            room.join(new TeamLeaderId(GUEST_ID), "게스트", GUEST_ACTION_TOKEN);
            room.selectDraftPosition(new TeamLeaderId(HOST_ID), 1);

            assertThat(room.getStartReadiness()).isEqualTo(RoomStartReadiness.WAITING_FOR_DRAFT_POSITIONS);
            assertThatThrownBy(() -> room.start(new TeamLeaderId(HOST_ID), deterministicGameId(room), STARTED_AT))
                .isInstanceOf(CoreException.class)
                .satisfies(ex -> {
                    CoreException coreException = (CoreException) ex;
                    assertThat(coreException.getError()).isEqualTo(RoomErrorType.ROOM_DRAFT_POSITIONS_NOT_FULL);
                    assertThat(coreException.getData()).isNull();
                });
        }

        @Test
        void 팀장_자리가_다_차지_않으면_시작할_수_없다() {
            Room room = auctionWaitingRoom();

            assertThatThrownBy(() -> room.start(new TeamLeaderId(HOST_ID), deterministicGameId(room), STARTED_AT))
                .isInstanceOf(CoreException.class)
                .satisfies(ex -> {
                    CoreException coreException = (CoreException) ex;
                    assertThat(coreException.getError()).isEqualTo(RoomErrorType.ROOM_LEADERS_NOT_FULL);
                    assertThat(coreException.getData()).isNull();
                });
        }

        @Test
        void 호스트가_아니면_시작할_수_없다() {
            Room room = auctionWaitingRoom();
            room.join(new TeamLeaderId(GUEST_ID), "게스트", GUEST_ACTION_TOKEN);

            assertThatThrownBy(() -> room.start(new TeamLeaderId(GUEST_ID), deterministicGameId(room), STARTED_AT))
                .isInstanceOf(CoreException.class)
                .satisfies(ex -> {
                    CoreException coreException = (CoreException) ex;
                    assertThat(coreException.getError()).isEqualTo(RoomErrorType.ROOM_START_FORBIDDEN);
                    assertThat(coreException.getData()).isNull();
                });
        }
    }

    private static Room auctionWaitingRoom() {
        return auctionWaitingRoom(CREATED_AT);
    }

    private static Room auctionWaitingRoom(Instant createdAt) {
        return Room.createFromTemplate(
            "ROOM01",
            new TeamLeaderId(HOST_ID),
            "호스트",
            HOST_ACTION_TOKEN,
            new RoomTemplateSpec(
                "LEAGUE_OF_LEGENDS",
                RoomMode.AUCTION,
                2,
                2,
                300,
                15,
                10,
                null,
                List.of(
                    new RoomTemplateSpec.Player(new RoomPlayerId(0), "선수1", "TOP", 0),
                    new RoomTemplateSpec.Player(new RoomPlayerId(1), "선수2", "JUNGLE", 1)
                )
            ),
            createdAt
        );
    }

    private static Room startedAuctionRoom() {
        Room room = auctionWaitingRoom();
        room.join(new TeamLeaderId(GUEST_ID), "게스트", GUEST_ACTION_TOKEN);
        room.start(new TeamLeaderId(HOST_ID), deterministicGameId(room), STARTED_AT);
        return room;
    }

    private static GameId deterministicGameId(Room room) {
        String source = "game:%s".formatted(room.getId().roomId());
        return new GameId(UUID.nameUUIDFromBytes(source.getBytes(StandardCharsets.UTF_8)));
    }

    private static Room waitingDraftRoom() {
        return waitingDraftRoom(CREATED_AT);
    }

    private static Room waitingDraftRoom(Instant createdAt) {
        return Room.createFromTemplate(
            "ROOM02",
            new TeamLeaderId(HOST_ID),
            "호스트",
            HOST_ACTION_TOKEN,
            new RoomTemplateSpec(
                "LEAGUE_OF_LEGENDS",
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
