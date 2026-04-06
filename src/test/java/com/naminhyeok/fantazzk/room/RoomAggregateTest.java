package com.naminhyeok.fantazzk.room;

import static org.assertj.core.api.Assertions.assertThat;

import com.naminhyeok.fantazzk.room.domain.DraftOrderStrategy;
import com.naminhyeok.fantazzk.room.domain.PlayerStatus;
import com.naminhyeok.fantazzk.room.domain.Room;
import com.naminhyeok.fantazzk.room.domain.RoomBid;
import com.naminhyeok.fantazzk.room.domain.RoomPlayer;
import com.naminhyeok.fantazzk.room.domain.RoomStatus;
import com.naminhyeok.fantazzk.room.domain.RoomTeamLeader;
import com.naminhyeok.fantazzk.room.domain.RoomTeamMember;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class RoomAggregateTest {

    @Test
    void room_uses_uuid_backed_room_id_and_root_room_code_value_object() {
        Room room = Room.createAuction("ROOM01", "host-external", 2, 2, 300);

        assertThat(room.getRoomId()).isNotNull();
        assertThat(room.getRoomId().getValue()).isInstanceOf(UUID.class);
        assertThat(room.getId()).isEqualTo(room.getRoomId());
        assertThat(room.getRoomCode()).isEqualTo(RoomCode.of("ROOM01"));
        assertThat(room.getCode()).isEqualTo("ROOM01");
    }

    @Test
    void create_methods_do_not_prepopulate_placeholder_players() {
        Room auctionRoom = Room.createAuction("ROOM03", "host-external", 2, 2, 300);
        Room draftRoom = Room.createDraft("ROOM04", "host-external", 2, 2, DraftOrderStrategy.SNAKE);

        assertThat(auctionRoom.getPlayers()).isEmpty();
        assertThat(draftRoom.getPlayers()).isEmpty();
    }

    @Test
    void auction_room_join_start_bid_and_settlement_are_owned_by_the_aggregate() {
        RoomId roomId = RoomId.random();
        Room room = Room.restore(
                roomId,
                "ROOM02",
                "host-external",
                RoomStatus.WAITING,
                com.naminhyeok.fantazzk.room.domain.TeamBuildingMode.AUCTION,
                2,
                2,
                300,
                null,
                null,
                null,
                List.of(
                        RoomPlayer.restore(null, roomId, "선수1", PlayerStatus.AVAILABLE, 0, Instant.parse("2025-01-01T00:00:00Z"), Instant.parse("2025-01-01T00:00:00Z")),
                        RoomPlayer.restore(null, roomId, "선수2", PlayerStatus.AVAILABLE, 1, Instant.parse("2025-01-01T00:00:00Z"), Instant.parse("2025-01-01T00:00:00Z"))),
                List.of(),
                List.of(),
                List.of(),
                Instant.parse("2025-01-01T00:00:00Z"),
                Instant.parse("2025-01-01T00:00:00Z"));

        room.join("호스트", room.getHostId());
        room.join("게스트", "leader-A");
        room.start();
        room.placeBid(room.getHostId(), 100);
        room.placeBid("leader-A", 120);
        room.settleAuction();

        assertThat(room.getStatus()).isEqualTo(RoomStatus.IN_PROGRESS);
        assertThat(room.getCurrentAuctionRound()).isEqualTo(2);
        assertThat(room.getPlayers())
                .filteredOn(player -> player.getStatus() == PlayerStatus.ASSIGNED)
                .singleElement()
                .satisfies(player -> {
            assertThat(player.getStatus()).isEqualTo(PlayerStatus.ASSIGNED);
            assertThat(player.getName()).isEqualTo("선수1");
        });
        assertThat(room.getMembers()).singleElement().satisfies(member -> {
            assertThat(member.getTeamLeaderId()).isEqualTo("leader-A");
            assertThat(member.getPlayerName()).isEqualTo("선수1");
            assertThat(member.getRoomId()).isEqualTo(room.getRoomId());
            assertThat(member.getRoomTeamMemberId()).isNotNull();
        });
        assertThat(room.getLeaders())
                .filteredOn(leader -> leader.getTeamLeaderId().equals("leader-A"))
                .singleElement()
                .satisfies(leader -> assertThat(leader.getRemainingBudget()).isEqualTo(180));
        assertThat(room.bidHistory())
                .extracting(RoomBid::getAmount)
                .containsExactly(100, 120);
    }

    @Test
    void draft_room_pick_flow_completes_using_restored_snapshot_state() {
        RoomId roomId = RoomId.random();
        Room draftRoom = Room.restore(
                roomId,
                "DRFT01",
                "host-external",
                RoomStatus.WAITING,
                com.naminhyeok.fantazzk.room.domain.TeamBuildingMode.DRAFT,
                2,
                2,
                null,
                DraftOrderStrategy.SNAKE,
                null,
                null,
                List.of(
                        RoomPlayer.restore(null, roomId, "선수1", PlayerStatus.AVAILABLE, 0, Instant.parse("2025-01-01T00:00:00Z"), Instant.parse("2025-01-01T00:00:00Z")),
                        RoomPlayer.restore(null, roomId, "선수2", PlayerStatus.AVAILABLE, 1, Instant.parse("2025-01-01T00:00:00Z"), Instant.parse("2025-01-01T00:00:00Z"))),
                List.of(
                        RoomTeamLeader.restore(null, roomId, "host-external", "호스트", null, Instant.parse("2025-01-01T00:00:00Z"), Instant.parse("2025-01-01T00:00:00Z")),
                        RoomTeamLeader.restore(null, roomId, "leader-A", "게스트", null, Instant.parse("2025-01-01T00:00:00Z"), Instant.parse("2025-01-01T00:00:00Z"))),
                List.of(),
                List.of(),
                Instant.parse("2025-01-01T00:00:00Z"),
                Instant.parse("2025-01-01T00:00:00Z"));

        draftRoom.start();
        draftRoom.pick("host-external", "선수1");
        draftRoom.pick("leader-A", "선수2");

        assertThat(draftRoom.getStatus()).isEqualTo(RoomStatus.COMPLETED);
        assertThat(draftRoom.getMembers())
                .extracting(RoomTeamMember::getPlayerName)
                .containsExactly("선수1", "선수2");
        assertThat(draftRoom.getPlayers())
                .extracting(RoomPlayer::getStatus)
                .containsExactly(PlayerStatus.ASSIGNED, PlayerStatus.ASSIGNED);
    }

    @Test
    void restored_room_detaches_from_mutable_input_snapshots() {
        RoomId roomId = RoomId.random();
        RoomPlayer originalPlayer = RoomPlayer.restore(
                null,
                roomId,
                "선수1",
                PlayerStatus.AVAILABLE,
                0,
                Instant.parse("2025-01-01T00:00:00Z"),
                Instant.parse("2025-01-01T00:00:00Z"));
        List<RoomPlayer> players = new ArrayList<>(List.of(originalPlayer));

        Room room = Room.restore(
                roomId,
                "SNAP01",
                "host-external",
                RoomStatus.WAITING,
                com.naminhyeok.fantazzk.room.domain.TeamBuildingMode.AUCTION,
                2,
                2,
                300,
                null,
                null,
                null,
                players,
                List.of(),
                List.of(),
                List.of(),
                Instant.parse("2025-01-01T00:00:00Z"),
                Instant.parse("2025-01-01T00:00:00Z"));

        players.clear();
        originalPlayer.assign();

        assertThat(room.getPlayers()).hasSize(1);
        assertThat(room.getPlayers().getFirst().getStatus()).isEqualTo(PlayerStatus.AVAILABLE);
    }
}
