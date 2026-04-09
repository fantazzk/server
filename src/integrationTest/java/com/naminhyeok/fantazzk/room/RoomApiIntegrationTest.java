package com.naminhyeok.fantazzk.room;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.boot.resttestclient.TestRestTemplate;
import org.springframework.boot.resttestclient.autoconfigure.AutoConfigureTestRestTemplate;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.TestConstructor;

@SpringBootTest(
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
    properties = {
        "spring.datasource.url=jdbc:h2:mem:room-api-test;DB_CLOSE_DELAY=-1",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.datasource.username=sa",
        "spring.datasource.password=",
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "spring.liquibase.enabled=false",
        "sentry.enabled=false"
    }
)
@AutoConfigureTestRestTemplate
@TestConstructor(autowireMode = TestConstructor.AutowireMode.ALL)
@RequiredArgsConstructor
class RoomApiIntegrationTest {
    private final TestRestTemplate restTemplate;
    private final Rooms rooms;

    @Test
    void list는_joinable_waiting_room만_응답하고_정렬과_JSON_필드를_보장한다() {
        rooms.save(joinableAuctionRoom("ROOM99", Instant.parse("2026-04-09T00:03:00Z")));
        rooms.save(fullWaitingAuctionRoom("ROOM08", Instant.parse("2026-04-09T00:02:00Z")));
        rooms.save(inProgressAuctionRoom("ROOM07", Instant.parse("2026-04-09T00:01:00Z")));
        rooms.save(joinableDraftRoom("ROOM01", Instant.parse("2026-04-09T00:00:00Z")));

        var response = restTemplate.getForEntity("/api/v1/rooms", JoinableRoomListApiResponse.class);
        JoinableRoomListApiResponse body = response.getBody();

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(body).isNotNull();
        assertThat(body.resultType()).isEqualTo("SUCCESS");
        assertThat(body.success()).hasSize(2);
        assertThat(body.success())
            .extracting(
                JoinableRoomResponse::code,
                JoinableRoomResponse::mode,
                JoinableRoomResponse::teamCount,
                JoinableRoomResponse::joinedLeaderCount,
                JoinableRoomResponse::remainingSlotCount,
                JoinableRoomResponse::startReadiness
            )
            .containsExactly(
                org.assertj.core.groups.Tuple.tuple(
                    "ROOM99",
                    "AUCTION",
                    2,
                    1,
                    1,
                    "WAITING_FOR_LEADERS"
                ),
                org.assertj.core.groups.Tuple.tuple(
                    "ROOM01",
                    "DRAFT",
                    2,
                    1,
                    1,
                    "WAITING_FOR_LEADERS"
                )
            );
    }

    private Room joinableAuctionRoom(String code, Instant createdAt) {
        return Room.createFromTemplate(
            code,
            "host-" + code,
            "호스트-" + code,
            "host-action-token-" + code,
            new RoomTemplateSpec(
                RoomTemplateSpec.Mode.AUCTION,
                2,
                2,
                300,
                null,
                List.of(
                    new RoomTemplateSpec.Player("선수1", 0),
                    new RoomTemplateSpec.Player("선수2", 1)
                )
            ),
            createdAt
        );
    }

    private Room fullWaitingAuctionRoom(String code, Instant createdAt) {
        Room room = joinableAuctionRoom(code, createdAt);
        room.join("guest-" + code, "게스트-" + code, "guest-action-token-" + code);
        return room;
    }

    private Room inProgressAuctionRoom(String code, Instant createdAt) {
        Room room = fullWaitingAuctionRoom(code, createdAt);
        room.start("host-" + code);
        return room;
    }

    private Room joinableDraftRoom(String code, Instant createdAt) {
        return Room.createFromTemplate(
            code,
            "host-" + code,
            "호스트-" + code,
            "host-action-token-" + code,
            new RoomTemplateSpec(
                RoomTemplateSpec.Mode.DRAFT,
                2,
                2,
                null,
                RoomTemplateSpec.DraftOrderStrategy.SNAKE,
                List.of(
                    new RoomTemplateSpec.Player("선수1", 0),
                    new RoomTemplateSpec.Player("선수2", 1)
                )
            ),
            createdAt
        );
    }

    private record JoinableRoomListApiResponse(
        String resultType,
        List<JoinableRoomResponse> success,
        Object error
    ) {
    }
}
