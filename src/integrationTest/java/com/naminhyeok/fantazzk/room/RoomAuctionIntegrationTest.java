package com.naminhyeok.fantazzk.room;

import static org.assertj.core.api.Assertions.assertThat;

import com.naminhyeok.fantazzk.template.TemplateFixture;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestConstructor;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest(
    properties = {
        "spring.datasource.url=jdbc:h2:mem:room-auction-test;DB_CLOSE_DELAY=-1",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.datasource.username=sa",
        "spring.datasource.password=",
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "spring.liquibase.enabled=false",
        "sentry.enabled=false"
    }
)
@TestConstructor(autowireMode = TestConstructor.AutowireMode.ALL)
@RequiredArgsConstructor
class RoomAuctionIntegrationTest {
    private final TemplateFixture templateFixture;
    private final CreateRoom createRoom;
    private final JoinRoom joinRoom;
    private final StartRoom startRoom;
    private final PlaceBid placeBid;
    private final SettleAuction settleAuction;
    private final Rooms rooms;

    @Test
    @Transactional
    void 입찰과_정산을_처리하면_선수_배정과_예산_차감이_반영된다() {
        var template =
            templateFixture.createAuctionTemplateId("경매전", 2, 2, 300, List.of("선수1", "선수2"));

        Room created = createRoom.create(template, "호스트");
        RoomTeamLeader guest = joinRoom.join(created.getCode(), "게스트");
        startRoom.start(created.getCode(), created.getLeaders().getFirst().getActionToken());

        placeBid.place(created.getCode(), guest.getId().value(), 150);
        AuctionSettlement settlement = settleAuction.settle(created.getCode());

        Room reloaded = rooms.findByCode(created.getCode()).orElseThrow();

        assertThat(settlement.outcome()).isEqualTo(AuctionOutcome.SOLD);
        assertThat(settlement.playerName()).isEqualTo("선수1");
        assertThat(reloaded.getMembers()).singleElement()
            .extracting(RoomTeamMember::teamLeaderId, RoomTeamMember::playerName)
            .containsExactly(guest.getId(), "선수1");
        assertThat(reloaded.getLeaders().stream().filter(it -> it.getId().equals(guest.getId())).findFirst().orElseThrow().getRemainingBudget())
            .isEqualTo(150);
        assertThat(reloaded.getCurrentAuctionRound()).isEqualTo(2);
    }

    @Test
    void 같은_라운드의_입찰_순번은_재조회_후에도_누적된다() {
        var template =
            templateFixture.createAuctionTemplateId("경매전", 2, 2, 300, List.of("선수1", "선수2"));

        Room created = createRoom.create(template, "호스트");
        RoomTeamLeader guest = joinRoom.join(created.getCode(), "게스트");
        startRoom.start(created.getCode(), created.getLeaders().getFirst().getActionToken());

        RoomBid firstBid = placeBid.place(created.getCode(), created.getLeaders().getFirst().getId().value(), 100);
        Room reloaded = rooms.findByCode(created.getCode()).orElseThrow();
        RoomBid secondBid = placeBid.place(reloaded.getCode(), guest.getId().value(), 150);

        assertThat(firstBid.sequence()).isEqualTo(new BidSequence(1));
        assertThat(secondBid.sequence()).isEqualTo(new BidSequence(2));
        assertThat(secondBid.round()).isEqualTo(1);
    }
}
