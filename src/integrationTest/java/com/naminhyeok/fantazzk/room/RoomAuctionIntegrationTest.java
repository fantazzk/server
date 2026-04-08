package com.naminhyeok.fantazzk.room;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.UUID;
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
    private final com.naminhyeok.fantazzk.template.TemplateManagement templateManagement;
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
            templateManagement.create(
                new com.naminhyeok.fantazzk.template.CreateTemplateInput(
                    "경매전",
                    com.naminhyeok.fantazzk.template.TemplateCatalog.Mode.AUCTION,
                    2,
                    2,
                    300,
                    null,
                    List.of("선수1", "선수2")
                )
            );

        Room created = createRoom.create(UUID.fromString(template.id()), "호스트");
        RoomTeamLeader guest = joinRoom.join(created.getCode(), "게스트");
        startRoom.start(created.getCode());

        placeBid.place(created.getCode(), guest.getTeamLeaderId(), 150);
        AuctionSettlement settlement = settleAuction.settle(created.getCode());

        Room reloaded = rooms.findByCode(created.getCode()).orElseThrow();

        assertThat(settlement.outcome()).isEqualTo(AuctionOutcome.SOLD);
        assertThat(settlement.playerName()).isEqualTo("선수1");
        assertThat(reloaded.getMembers()).singleElement()
            .extracting(RoomTeamMember::getTeamLeaderId, RoomTeamMember::getPlayerName)
            .containsExactly(guest.getTeamLeaderId(), "선수1");
        assertThat(reloaded.getLeaders().stream().filter(it -> it.getTeamLeaderId().equals(guest.getTeamLeaderId())).findFirst().orElseThrow().getRemainingBudget())
            .isEqualTo(150);
        assertThat(reloaded.getCurrentAuctionRound()).isEqualTo(2);
    }
}
