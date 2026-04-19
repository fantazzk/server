package com.naminhyeok.fantazzk.room;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.naminhyeok.fantazzk.CoreException;
import com.naminhyeok.fantazzk.room.domain.AuctionBid;
import com.naminhyeok.fantazzk.room.domain.AuctionGame;
import com.naminhyeok.fantazzk.room.domain.AuctionOutcome;
import com.naminhyeok.fantazzk.room.domain.AuctionSettlement;
import com.naminhyeok.fantazzk.room.domain.BidSequence;
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

class AuctionGameTest {
    private static final Instant STARTED_AT = Instant.parse("2026-04-15T00:00:00Z");
    private static final int PICK_BAN_TIME = 45;
    private static final int MIN_BID_UNIT = 10;
    private static final TeamLeaderId HOST_ID = new TeamLeaderId("host-1");
    private static final TeamLeaderId GUEST_ID = new TeamLeaderId("guest-1");

    @Test
    void 경매_게임은_입찰과_정산으로_라운드와_멤버와_예산을_직접_갱신한다() {
        AuctionGame game = startedAuctionGameWithTwoPlayers();

        AuctionBid firstBid = game.placeBid(HOST_ID, 100, STARTED_AT.plusSeconds(1));
        AuctionBid secondBid = game.placeBid(GUEST_ID, 150, STARTED_AT.plusSeconds(2));

        AuctionSettlement settlement = game.settleAuction(STARTED_AT.plusSeconds(2 + PICK_BAN_TIME));

        assertThat(firstBid.sequence()).isEqualTo(new BidSequence(1));
        assertThat(secondBid.sequence()).isEqualTo(new BidSequence(2));
        assertThat(settlement).isEqualTo(new AuctionSettlement("선수1", AuctionOutcome.SOLD));
        assertThat(game.getMembers()).singleElement()
            .extracting(RosterMember::teamLeaderId, RosterMember::playerName)
            .containsExactly(GUEST_ID, "선수1");
        assertThat(game.getParticipants())
            .extracting(GameParticipant::teamLeaderId, GameParticipant::remainingBudget)
            .containsExactly(
                org.assertj.core.groups.Tuple.tuple(HOST_ID, 300),
                org.assertj.core.groups.Tuple.tuple(GUEST_ID, 150)
            );
        assertThat(game.getCurrentRound()).isEqualTo(2);
        assertThat(game.getCurrentRoundEndsAt()).isEqualTo(STARTED_AT.plusSeconds(2 + PICK_BAN_TIME * 2L));
        assertThat(game.currentAuctionTarget()).extracting(GamePlayer::name).isEqualTo("선수2");
        assertThat(game.currentWinningBid()).isNull();
        assertThat(game.currentBidCount()).isZero();
    }

    @Test
    void 경매_게임은_유찰되면_대상을_뒤로_보내고_다음_라운드를_연다() {
        AuctionGame game = startedAuctionGameWithTwoPlayers();

        AuctionSettlement settlement = game.settleAuction(STARTED_AT.plusSeconds(PICK_BAN_TIME));

        assertThat(settlement).isEqualTo(new AuctionSettlement("선수1", AuctionOutcome.PASSED));
        assertThat(game.getCurrentRound()).isEqualTo(2);
        assertThat(game.currentAuctionTarget()).extracting(GamePlayer::name).isEqualTo("선수2");
        assertThat(game.getMembers()).isEmpty();
    }

    @Test
    void 경매_게임은_deadline이_지나면_입찰을_막는다() {
        AuctionGame game = startedAuctionGame();

        assertThatThrownBy(() -> game.placeBid(HOST_ID, 100, STARTED_AT.plusSeconds(PICK_BAN_TIME)))
            .isInstanceOf(CoreException.class)
            .isInstanceOfSatisfying(
                CoreException.class,
                ex -> assertThat(ex.getError()).isEqualTo(RoomErrorType.ROOM_BID_REQUIRES_OPEN_ROUND)
            );
    }

    @Test
    void 경매_게임은_입찰하면_deadline을_입찰_시점부터_다시_연장한다() {
        AuctionGame game = startedAuctionGame();

        game.placeBid(HOST_ID, 100, STARTED_AT.plusSeconds(10));

        assertThat(game.getCurrentRoundEndsAt()).isEqualTo(STARTED_AT.plusSeconds(10 + PICK_BAN_TIME));
    }

    @Test
    void 마지막_선수를_정산하면_경매_게임은_완료_상태가_된다() {
        AuctionGame game = startedAuctionGameWithTwoPlayers();

        game.placeBid(HOST_ID, 100, STARTED_AT.plusSeconds(1));
        game.settleAuction(STARTED_AT.plusSeconds(1 + PICK_BAN_TIME));
        game.placeBid(GUEST_ID, 110, STARTED_AT.plusSeconds(1 + PICK_BAN_TIME + 1L));
        AuctionSettlement settlement = game.settleAuction(STARTED_AT.plusSeconds(2 + PICK_BAN_TIME * 2L));

        assertThat(settlement).isEqualTo(new AuctionSettlement("선수2", AuctionOutcome.SOLD));
        assertThat(game.getStatus()).isEqualTo(GameStatus.COMPLETED);
        assertThat(game.getCurrentRoundEndsAt()).isNull();
        assertThat(game.currentAuctionTarget()).isNull();
        assertThat(game.currentWinningBid()).isNull();
        assertThat(game.currentBidCount()).isZero();
    }

    private AuctionGame startedAuctionGame() {
        return new AuctionGame(
            new GameId(UUID.fromString("00000000-0000-0000-0000-000000000101")),
            new RoomId(UUID.fromString("00000000-0000-0000-0000-000000000001")),
            "ROOM01",
            STARTED_AT,
            GameRules.auction(2, 2, 300, PICK_BAN_TIME, MIN_BID_UNIT, null),
            List.of(
                GameParticipant.auction(HOST_ID, "호스트", 300),
                GameParticipant.auction(GUEST_ID, "게스트", 300)
            ),
            List.of(
                new GamePlayer(new RoomPlayerId(0), "선수1", "TOP", 0)
            ),
            1,
            STARTED_AT.plusSeconds(PICK_BAN_TIME)
        );
    }

    private AuctionGame startedAuctionGameWithTwoPlayers() {
        return new AuctionGame(
            new GameId(UUID.fromString("00000000-0000-0000-0000-000000000102")),
            new RoomId(UUID.fromString("00000000-0000-0000-0000-000000000002")),
            "ROOM02",
            STARTED_AT,
            GameRules.auction(2, 2, 300, PICK_BAN_TIME, MIN_BID_UNIT, null),
            List.of(
                GameParticipant.auction(HOST_ID, "호스트", 300),
                GameParticipant.auction(GUEST_ID, "게스트", 300)
            ),
            List.of(
                new GamePlayer(new RoomPlayerId(0), "선수1", "TOP", 0),
                new GamePlayer(new RoomPlayerId(1), "선수2", "JUNGLE", 1)
            ),
            1,
            STARTED_AT.plusSeconds(PICK_BAN_TIME)
        );
    }
}
