package com.naminhyeok.fantazzk.room.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.naminhyeok.fantazzk.CoreException;
import com.naminhyeok.fantazzk.room.domain.DraftGame;
import com.naminhyeok.fantazzk.room.domain.DraftOrderStrategy;
import com.naminhyeok.fantazzk.room.domain.DraftProgress;
import com.naminhyeok.fantazzk.room.domain.GameId;
import com.naminhyeok.fantazzk.room.domain.GameParticipant;
import com.naminhyeok.fantazzk.room.domain.GamePlayer;
import com.naminhyeok.fantazzk.room.domain.GameRules;
import com.naminhyeok.fantazzk.room.domain.GameStatus;
import com.naminhyeok.fantazzk.room.domain.RoomErrorType;
import com.naminhyeok.fantazzk.room.domain.RoomId;
import com.naminhyeok.fantazzk.room.domain.RoomPlayerId;
import com.naminhyeok.fantazzk.room.domain.RosterMember;
import com.naminhyeok.fantazzk.room.domain.TeamLeaderId;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class DraftGameTest {
    private static final Instant STARTED_AT = Instant.parse("2026-04-15T00:00:00Z");
    private static final TeamLeaderId HOST_ID = new TeamLeaderId("host-1");
    private static final TeamLeaderId GUEST_ID = new TeamLeaderId("guest-1");

    @Test
    void 드래프트_게임은_픽으로_턴과_멤버와_선수_가용상태를_직접_갱신한다() {
        DraftGame game = startedDraftGame();

        RosterMember member = game.pick(HOST_ID, "선수1");

        assertThat(member).isEqualTo(new RosterMember(HOST_ID, "선수1", 0));
        assertThat(game.getCurrentTurnIndex()).isEqualTo(1);
        assertThat(game.currentDraftProgress())
            .extracting(DraftProgress::currentRound, DraftProgress::currentLeaderId, DraftProgress::currentRoundLeaderIds)
            .containsExactly(1, GUEST_ID.value(), List.of(HOST_ID.value(), GUEST_ID.value()));
        assertThat(game.getMembers()).containsExactly(member);
        assertThat(game.getPlayerPool())
            .extracting(GamePlayer::name)
            .containsExactly("선수1", "선수2");
        assertThat(game.isPlayerAvailable("선수1")).isFalse();
        assertThat(game.isPlayerAvailable("선수2")).isTrue();
    }

    @Test
    void SNAKE_드래프트_게임은_2라운드에서_역순으로_현재_팀장을_계산한다() {
        DraftGame game = startedDraftGame(DraftOrderStrategy.SNAKE, 3, List.of("선수1", "선수2", "선수3", "선수4"));

        game.pick(HOST_ID, "선수1");
        game.pick(GUEST_ID, "선수2");

        assertThat(game.currentDraftProgress())
            .extracting(DraftProgress::currentRound, DraftProgress::currentLeaderId, DraftProgress::currentRoundLeaderIds)
            .containsExactly(2, GUEST_ID.value(), List.of(GUEST_ID.value(), HOST_ID.value()));
    }

    @Test
    void FIXED_드래프트_게임은_2라운드에서도_같은_순서로_현재_팀장을_계산한다() {
        DraftGame game = startedDraftGame(DraftOrderStrategy.FIXED, 3, List.of("선수1", "선수2", "선수3", "선수4"));

        game.pick(HOST_ID, "선수1");
        game.pick(GUEST_ID, "선수2");

        assertThat(game.currentDraftProgress())
            .extracting(DraftProgress::currentRound, DraftProgress::currentLeaderId, DraftProgress::currentRoundLeaderIds)
            .containsExactly(2, HOST_ID.value(), List.of(HOST_ID.value(), GUEST_ID.value()));
    }

    @Test
    void 마지막_픽을_완료하면_드래프트_게임은_완료된다() {
        DraftGame game = startedDraftGame();

        game.pick(HOST_ID, "선수1");
        RosterMember secondPick = game.pick(GUEST_ID, "선수2");

        assertThat(secondPick).isEqualTo(new RosterMember(GUEST_ID, "선수2", 1));
        assertThat(game.getStatus()).isEqualTo(GameStatus.COMPLETED);
        assertThat(game.currentDraftProgress()).isNull();
        assertThat(game.getMembers()).containsExactly(
            new RosterMember(HOST_ID, "선수1", 0),
            secondPick
        );
    }

    @Test
    void 자신의_턴이_아니면_드래프트_게임은_픽을_막는다() {
        DraftGame game = startedDraftGame();

        assertThatThrownBy(() -> game.pick(GUEST_ID, "선수1"))
            .isInstanceOf(CoreException.class)
            .isInstanceOfSatisfying(
                CoreException.class,
                ex -> assertThat(ex.getError()).isEqualTo(RoomErrorType.ROOM_PICK_OUT_OF_TURN)
            );
    }

    @Test
    void 이미_선택된_선수는_드래프트_게임에서_다시_픽할_수_없다() {
        DraftGame game = startedDraftGame();
        game.pick(HOST_ID, "선수1");

        assertThatThrownBy(() -> game.pick(GUEST_ID, "선수1"))
            .isInstanceOf(CoreException.class)
            .isInstanceOfSatisfying(
                CoreException.class,
                ex -> assertThat(ex.getError()).isEqualTo(RoomErrorType.ROOM_PICK_PLAYER_NOT_AVAILABLE)
            );
    }

    private DraftGame startedDraftGame() {
        return startedDraftGame(DraftOrderStrategy.SNAKE, 2, List.of("선수1", "선수2"));
    }

    private DraftGame startedDraftGame(
        DraftOrderStrategy draftOrderStrategy,
        int teamSize,
        List<String> playerNames
    ) {
        return new DraftGame(
            new GameId(UUID.fromString("00000000-0000-0000-0000-000000000101")),
            new RoomId(UUID.fromString("00000000-0000-0000-0000-000000000001")),
            "ROOM01",
            STARTED_AT,
            GameRules.draft(2, teamSize, 30, draftOrderStrategy),
            List.of(
                GameParticipant.draft(HOST_ID, "호스트", 1),
                GameParticipant.draft(GUEST_ID, "게스트", 2)
            ),
            playerNames.stream()
                .map(name -> new GamePlayer(new RoomPlayerId(playerNames.indexOf(name)), name, "TOP", playerNames.indexOf(name)))
                .toList(),
            0
        );
    }
}
