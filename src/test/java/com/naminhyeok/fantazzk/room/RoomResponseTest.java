package com.naminhyeok.fantazzk.room;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.groups.Tuple.tuple;

import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.Test;

class RoomResponseTest {
    private static final Instant CREATED_AT = Instant.parse("2026-04-09T00:00:00Z");
    private static final String HOST_ID = "host-1";
    private static final String HOST_ACTION_TOKEN = "host-action-token";
    private static final String GUEST_ONE_ID = "guest-1";
    private static final String GUEST_ONE_ACTION_TOKEN = "guest-1-action-token";
    private static final String GUEST_TWO_ID = "guest-2";
    private static final String GUEST_TWO_ACTION_TOKEN = "guest-2-action-token";

    @Test
    void 드래프트_방_응답은_자리_기준_예상_시작_순서를_슬롯으로_반환한다() {
        Room room = threeTeamDraftRoom();
        room.join(new TeamLeaderId(GUEST_ONE_ID), "게스트1", GUEST_ONE_ACTION_TOKEN);
        room.join(new TeamLeaderId(GUEST_TWO_ID), "게스트2", GUEST_TWO_ACTION_TOKEN);
        room.selectDraftPosition(new TeamLeaderId(HOST_ID), 2);
        room.selectDraftPosition(new TeamLeaderId(GUEST_ONE_ID), 1);
        room.selectDraftPosition(new TeamLeaderId(GUEST_TWO_ID), 3);

        RoomResponse response = RoomResponse.from(room);

        assertThat(response.draftOrderPreview()).isNotNull();
        assertThat(response.draftOrderPreview().slots())
            .extracting(DraftOrderSlotResponse::draftPosition, DraftOrderSlotResponse::leaderId, DraftOrderSlotResponse::nickname)
            .containsExactly(
                tuple(1, GUEST_ONE_ID, "게스트1"),
                tuple(2, HOST_ID, "호스트"),
                tuple(3, GUEST_TWO_ID, "게스트2")
            );
    }

    @Test
    void 드래프트_방_응답은_비어있는_자리를_null_슬롯으로_반환한다() {
        Room room = threeTeamDraftRoom();
        room.join(new TeamLeaderId(GUEST_ONE_ID), "게스트1", GUEST_ONE_ACTION_TOKEN);
        room.selectDraftPosition(new TeamLeaderId(HOST_ID), 2);

        RoomResponse response = RoomResponse.from(room);

        assertThat(response.draftOrderPreview()).isNotNull();
        assertThat(response.draftOrderPreview().slots())
            .extracting(DraftOrderSlotResponse::draftPosition, DraftOrderSlotResponse::leaderId, DraftOrderSlotResponse::nickname)
            .containsExactly(
                tuple(1, null, null),
                tuple(2, HOST_ID, "호스트"),
                tuple(3, null, null)
            );
    }

    @Test
    void 경매_방_응답은_드래프트_preview를_포함하지_않는다() {
        Room room =
            Room.createFromTemplate(
                "AUC001",
                new TeamLeaderId(HOST_ID),
                "호스트",
                HOST_ACTION_TOKEN,
                new RoomTemplateSpec(
                    RoomTemplateSpec.Mode.AUCTION,
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
                CREATED_AT
            );

        RoomResponse response = RoomResponse.from(room);

        assertThat(response.draftOrderPreview()).isNull();
    }

    private Room threeTeamDraftRoom() {
        return Room.createFromTemplate(
            "DRF001",
            new TeamLeaderId(HOST_ID),
            "호스트",
            HOST_ACTION_TOKEN,
            new RoomTemplateSpec(
                RoomTemplateSpec.Mode.DRAFT,
                3,
                2,
                null,
                30,
                null,
                RoomTemplateSpec.DraftOrderStrategy.SNAKE,
                List.of(
                    new RoomTemplateSpec.Player(new RoomPlayerId(0), "선수1", "TOP", 0),
                    new RoomTemplateSpec.Player(new RoomPlayerId(1), "선수2", "JUNGLE", 1),
                    new RoomTemplateSpec.Player(new RoomPlayerId(2), "선수3", "MID", 2)
                )
            ),
            CREATED_AT
        );
    }
}
