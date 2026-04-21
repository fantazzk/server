package com.naminhyeok.fantazzk.room.web;

import static org.assertj.core.api.Assertions.assertThat;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.naminhyeok.fantazzk.room.domain.AuctionGame;
import com.naminhyeok.fantazzk.room.domain.DraftOrderStrategy;
import com.naminhyeok.fantazzk.room.domain.GameFactory;
import com.naminhyeok.fantazzk.room.domain.GameId;
import com.naminhyeok.fantazzk.room.domain.Room;
import com.naminhyeok.fantazzk.room.domain.RoomMode;
import com.naminhyeok.fantazzk.room.domain.RoomPlayerId;
import com.naminhyeok.fantazzk.room.domain.RoomTemplateSpec;
import com.naminhyeok.fantazzk.room.domain.StartedGameSnapshot;
import com.naminhyeok.fantazzk.room.domain.TeamLeaderId;
import com.naminhyeok.fantazzk.room.repository.Games;
import com.naminhyeok.fantazzk.room.repository.Rooms;
import com.naminhyeok.fantazzk.room.query.JoinableRoomResponse;
import java.nio.charset.StandardCharsets;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.lang.reflect.Field;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.boot.resttestclient.TestRestTemplate;
import org.springframework.boot.resttestclient.autoconfigure.AutoConfigureTestRestTemplate;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.TestConstructor;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

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
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper().findAndRegisterModules();

    private final TestRestTemplate restTemplate;
    private final Rooms rooms;
    private final Games games;
    private final PlatformTransactionManager transactionManager;

    @Test
    void list는_joinable_waiting_room만_응답하고_정렬과_JSON_필드를_보장한다() {
        rooms.save(joinableAuctionRoom("ROOM99", Instant.parse("2026-04-09T00:03:00Z")));
        rooms.save(fullWaitingAuctionRoom("ROOM08", Instant.parse("2026-04-09T00:02:00Z")));
        rooms.save(inProgressAuctionRoom("ROOM07", Instant.parse("2026-04-09T00:01:00Z")));
        rooms.save(joinableDraftRoom("ROOM01", Instant.parse("2026-04-09T00:00:00Z")));

        ResponseEntity<JoinableRoomListApiResponse> response = restTemplate.getForEntity("/api/v1/rooms", JoinableRoomListApiResponse.class);
        JoinableRoomListApiResponse body = response.getBody();

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(body).isNotNull();
        assertThat(body.resultType()).isEqualTo("SUCCESS");
        assertThat(body.success()).hasSize(2);
        assertThat(body.success()).extracting(JoinableRoomResponse::code).containsExactly("ROOM99", "ROOM01");
    }

    @Test
    void start는_game_id만_포함한_최소_응답을_반환한다() throws Exception {
        rooms.save(fullWaitingAuctionRoom("ROOM10", CREATED_AT));

        ResponseEntity<String> response = restTemplate.exchange(
            "/api/v1/rooms/ROOM10/start",
            HttpMethod.POST,
            new HttpEntity<>("", actionHeaders("host-action-token-ROOM10")),
            String.class
        );
        JsonNode body = readBody(response);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(body.at("/resultType").asText()).isEqualTo("SUCCESS");
        assertThat(UUID.fromString(body.at("/success/gameId").asText())).isNotNull();
        assertThat(body.at("/success/roomCode").isMissingNode()).isTrue();
        assertThat(body.at("/success/mode").isMissingNode()).isTrue();
        assertThat(body.at("/success/status").isMissingNode()).isTrue();
        assertThat(body.at("/error").isNull()).isTrue();
    }

    @Test
    void get_room은_startedGameId만_반환하고_live_game_progress는_포함하지_않는다() throws Exception {
        Room room = startedAuctionRoom("ROOM11", CREATED_AT);

        ResponseEntity<String> response = restTemplate.getForEntity("/api/v1/rooms/ROOM11", String.class);
        JsonNode body = readBody(response);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(body.at("/resultType").asText()).isEqualTo("SUCCESS");
        assertThat(body.at("/success/roomCode").asText()).isEqualTo("ROOM11");
        assertThat(body.at("/success/status").asText()).isEqualTo("STARTED");
        assertThat(body.at("/success/startedGameId").asText()).isEqualTo(room.getStartedGameId().gameId().toString());
        assertThat(body.at("/success/auctionProgress").isMissingNode()).isTrue();
        assertThat(body.at("/success/draftProgress").isMissingNode()).isTrue();
        assertThat(body.at("/success/members").isMissingNode()).isTrue();
    }

    @Test
    void get_game은_경매의_deadline_projection을_반환한다() throws Exception {
        Room room = startedAuctionRoom("ROOM12", CREATED_AT);

        restTemplate.exchange(
            "/api/v1/games/" + room.getStartedGameId().gameId() + "/bids",
            HttpMethod.POST,
            jsonRequest(
                """
                {
                  "amount": 120
                }
                """,
                "host-action-token-ROOM12"
            ),
            String.class
        );

        ResponseEntity<String> response =
            restTemplate.getForEntity("/api/v1/games/" + room.getStartedGameId().gameId(), String.class);
        JsonNode body = readBody(response);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(body.at("/resultType").asText()).isEqualTo("SUCCESS");
        assertThat(body.at("/success/gameId").asText()).isEqualTo(room.getStartedGameId().gameId().toString());
        assertThat(body.at("/success/roomCode").asText()).isEqualTo("ROOM12");
        assertThat(body.at("/success/status").asText()).isEqualTo("IN_PROGRESS");
        assertThat(body.at("/success/auctionProgress/currentRound").asInt()).isEqualTo(1);
        assertThat(body.at("/success/auctionProgress/currentAuctionTarget/name").asText()).isEqualTo("선수1");
        assertThat(body.at("/success/auctionProgress/currentAuctionTarget/position").asText()).isEqualTo("TOP");
        assertThat(body.at("/success/auctionProgress/highestBidAmount").asInt()).isEqualTo(120);
        assertThat(body.at("/success/auctionProgress/leadingLeaderId").asText()).isEqualTo("host-ROOM12");
        assertThat(body.at("/success/auctionProgress/bidCount").asInt()).isEqualTo(1);
        assertThat(body.at("/success/auctionProgress/currentAuctionRoundEndsAt").asText())
            .isEqualTo("2026-04-09T00:01:05Z");
    }

    @Test
    void get_game은_시작된_드래프트의_live_progress와_멤버와_선수상태를_반환한다() throws Exception {
        Room room = startedDraftRoom("ROOM14", CREATED_AT);
        restTemplate.exchange(
            "/api/v1/games/" + room.getStartedGameId().gameId() + "/draft-picks",
            HttpMethod.POST,
            jsonRequest(
                """
                {
                  "playerName": "선수1"
                }
                """,
                "host-action-token-ROOM14"
            ),
            String.class
        );

        ResponseEntity<String> response =
            restTemplate.getForEntity("/api/v1/games/" + room.getStartedGameId().gameId(), String.class);
        JsonNode body = readBody(response);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(body.at("/success/status").asText()).isEqualTo("IN_PROGRESS");
        assertThat(body.at("/success/roster/0/teamLeaderId").asText()).isEqualTo("host-ROOM14");
        assertThat(body.at("/success/roster/0/playerName").asText()).isEqualTo("선수1");
        assertThat(body.at("/success/playerPool/0/name").asText()).isEqualTo("선수1");
        assertThat(body.at("/success/playerPool/0/status").asText()).isEqualTo("ASSIGNED");
        assertThat(body.at("/success/playerPool/1/name").asText()).isEqualTo("선수2");
        assertThat(body.at("/success/playerPool/1/status").asText()).isEqualTo("AVAILABLE");
        assertThat(body.at("/success/draftProgress/currentTurnIndex").asInt()).isEqualTo(1);
        assertThat(body.at("/success/draftProgress/currentRound").asInt()).isEqualTo(1);
        assertThat(body.at("/success/draftProgress/currentLeaderId").asText()).isEqualTo("guest-ROOM14");
    }

    private Room startedAuctionRoom(String code, Instant createdAt) {
        Room room = Room.createFromTemplate(
            code,
            new TeamLeaderId("host-" + code),
            "호스트-" + code,
            "host-action-token-" + code,
            new RoomTemplateSpec(
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
            createdAt
        );
        room.join(new TeamLeaderId("guest-" + code), "게스트-" + code, "guest-action-token-" + code);
        GameId gameId = deterministicGameId(room);
        StartedGameSnapshot snapshot = room.start(new TeamLeaderId("host-" + code), gameId, createdAt);
        rooms.save(room);
        games.save(new GameFactory().create(snapshot));
        return room;
    }

    private static void setCurrentAuctionRoundEndsAt(AuctionGame game, Instant deadline) {
        try {
            Field field = AuctionGame.class.getDeclaredField("currentRoundEndsAt");
            field.setAccessible(true);
            field.set(game, deadline);
        } catch (ReflectiveOperationException ex) {
            throw new AssertionError(ex);
        }
    }

    private Room joinableAuctionRoom(String code, Instant createdAt) {
        return Room.createFromTemplate(
            code,
            new TeamLeaderId("host-" + code),
            "호스트-" + code,
            "host-action-token-" + code,
            new RoomTemplateSpec(
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
        room.start(new TeamLeaderId("host-" + code), deterministicGameId(room), createdAt);
        return room;
    }

    private Room joinableDraftRoom(String code, Instant createdAt) {
        return Room.createFromTemplate(
            code,
            new TeamLeaderId("host-" + code),
            "호스트-" + code,
            "host-action-token-" + code,
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

    private Room startedDraftRoom(String code, Instant createdAt) {
        Room room = joinableDraftRoom(code, createdAt);
        room.join(new TeamLeaderId("guest-" + code), "게스트-" + code, "guest-action-token-" + code);
        room.selectDraftPosition(new TeamLeaderId("host-" + code), 1);
        room.selectDraftPosition(new TeamLeaderId("guest-" + code), 2);
        GameId gameId = deterministicGameId(room);
        StartedGameSnapshot snapshot = room.start(new TeamLeaderId("host-" + code), gameId, createdAt);
        rooms.save(room);
        games.save(new GameFactory().create(snapshot));
        return room;
    }

    private static GameId deterministicGameId(Room room) {
        String source = "game:%s".formatted(room.getId().roomId());
        return new GameId(UUID.nameUUIDFromBytes(source.getBytes(StandardCharsets.UTF_8)));
    }

    private record JoinableRoomListApiResponse(
        String resultType,
        List<JoinableRoomResponse> success,
        Object error
    ) {
    }

    private static HttpHeaders actionHeaders(String actionToken) {
        HttpHeaders headers = jsonHeaders();
        headers.set("X-Room-Action-Token", actionToken);
        return headers;
    }

    private static HttpHeaders jsonHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        return headers;
    }

    private static HttpEntity<String> jsonRequest(String body, String actionToken) {
        return new HttpEntity<>(body, actionHeaders(actionToken));
    }

    private static JsonNode readBody(ResponseEntity<String> response) throws Exception {
        return OBJECT_MAPPER.readTree(response.getBody());
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
