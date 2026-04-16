package com.naminhyeok.fantazzk.room;

import static org.assertj.core.api.Assertions.assertThat;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.boot.resttestclient.TestRestTemplate;
import org.springframework.boot.resttestclient.autoconfigure.AutoConfigureTestRestTemplate;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
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
@Import(RoomApiIntegrationTest.FixedClockConfiguration.class)
@TestConstructor(autowireMode = TestConstructor.AutowireMode.ALL)
@RequiredArgsConstructor
class RoomApiIntegrationTest {
    private static final Instant CREATED_AT = Instant.parse("2026-04-09T00:00:00Z");

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
        assertThat(body.success()).extracting(JoinableRoomResponse::code).containsExactly("ROOM99", "ROOM01");
    }

    @Test
    void auctionProgress는_due된_경매를_정산한_latest_snapshot을_반환한다() {
        Room room = startedAuctionRoom("ROOM10", CREATED_AT.minusSeconds(30));
        room.placeBid(new TeamLeaderId("host-ROOM10"), 100, CREATED_AT.minusSeconds(30));
        rooms.save(room);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        var response = restTemplate.exchange(
            "/api/v1/rooms/ROOM10/auction/progress",
            HttpMethod.POST,
            new HttpEntity<>("", headers),
            RoomResponseApiResponse.class
        );
        RoomResponseApiResponse body = response.getBody();

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(body).isNotNull();
        assertThat(body.resultType()).isEqualTo("SUCCESS");
        assertThat(body.success().code()).isEqualTo("ROOM10");
        assertThat(body.success().status()).isEqualTo("IN_PROGRESS");
        assertThat(body.success().progress().currentRound()).isEqualTo(2);
        assertThat(body.success().progress().currentAuctionTarget().name()).isEqualTo("선수2");
        assertThat(body.success().progress().currentAuctionTarget().position()).isEqualTo("JUNGLE");
        assertThat(body.success().progress().highestBidAmount()).isNull();
        assertThat(body.success().progress().leadingLeaderId()).isNull();
        assertThat(body.success().progress().bidCount()).isEqualTo(0);
        assertThat(body.success().progress().currentAuctionRoundEndsAt())
            .isAfter(CREATED_AT.plusSeconds(45));
    }

    @Test
    void get은_경매의_deadline_projection을_반환한다() {
        Room room = startedAuctionRoom("ROOM11", CREATED_AT);
        room.placeBid(new TeamLeaderId("host-ROOM11"), 120, CREATED_AT.plusSeconds(1));
        rooms.save(room);

        var response = restTemplate.getForEntity("/api/v1/rooms/ROOM11", RoomResponseApiResponse.class);
        RoomResponseApiResponse body = response.getBody();

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(body).isNotNull();
        assertThat(body.resultType()).isEqualTo("SUCCESS");
        assertThat(body.success().code()).isEqualTo("ROOM11");
        assertThat(body.success().progress().currentRound()).isEqualTo(1);
        assertThat(body.success().progress().currentAuctionTarget().name()).isEqualTo("선수1");
        assertThat(body.success().progress().currentAuctionTarget().position()).isEqualTo("TOP");
        assertThat(body.success().progress().highestBidAmount()).isEqualTo(120);
        assertThat(body.success().progress().leadingLeaderId()).isEqualTo("host-ROOM11");
        assertThat(body.success().progress().bidCount()).isEqualTo(1);
        assertThat(body.success().progress().currentAuctionRoundEndsAt())
            .isEqualTo(Instant.parse("2026-04-09T00:00:46Z"));
    }

    private Room startedAuctionRoom(String code, Instant createdAt) {
        Room room = Room.createFromTemplate(
            code,
            new TeamLeaderId("host-" + code),
            "호스트-" + code,
            "host-action-token-" + code,
            new RoomTemplateSpec(
                RoomTemplateSpec.Mode.AUCTION,
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
            createdAt
        );
        room.join(new TeamLeaderId("guest-" + code), "게스트-" + code, "guest-action-token-" + code);
        room.start(new TeamLeaderId("host-" + code), createdAt);
        return room;
    }

    private Room joinableAuctionRoom(String code, Instant createdAt) {
        return Room.createFromTemplate(
            code,
            new TeamLeaderId("host-" + code),
            "호스트-" + code,
            "host-action-token-" + code,
            new RoomTemplateSpec(
                RoomTemplateSpec.Mode.AUCTION,
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
            createdAt
        );
    }

    private Room fullWaitingAuctionRoom(String code, Instant createdAt) {
        Room room = joinableAuctionRoom(code, createdAt);
        room.join(new TeamLeaderId("guest-" + code), "게스트-" + code, "guest-action-token-" + code);
        return room;
    }

    private Room inProgressAuctionRoom(String code, Instant createdAt) {
        Room room = fullWaitingAuctionRoom(code, createdAt);
        room.start(new TeamLeaderId("host-" + code), createdAt);
        return room;
    }

    private Room joinableDraftRoom(String code, Instant createdAt) {
        return Room.createFromTemplate(
            code,
            new TeamLeaderId("host-" + code),
            "호스트-" + code,
            "host-action-token-" + code,
            new RoomTemplateSpec(
                RoomTemplateSpec.Mode.DRAFT,
                2,
                2,
                null,
                30,
                null,
                RoomTemplateSpec.DraftOrderStrategy.SNAKE,
                List.of(
                    new RoomTemplateSpec.Player(new RoomPlayerId(0), "선수1", "TOP", 0),
                    new RoomTemplateSpec.Player(new RoomPlayerId(1), "선수2", "JUNGLE", 1)
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

    private record RoomResponseApiResponse(
        String resultType,
        RoomResponse success,
        Object error
    ) {
    }

    @TestConfiguration
    static class FixedClockConfiguration {
        @Bean
        @Primary
        Clock roomApiTestClock() {
            return Clock.fixed(Instant.parse("2026-04-09T00:00:20Z"), ZoneOffset.UTC);
        }
    }
}
