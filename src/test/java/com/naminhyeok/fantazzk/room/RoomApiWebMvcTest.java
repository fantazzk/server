package com.naminhyeok.fantazzk.room;

import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.assertj.core.api.Assertions.assertThat;

import com.naminhyeok.fantazzk.CoreException;
import com.naminhyeok.fantazzk.ErrorMessage;
import com.naminhyeok.fantazzk.GlobalExceptionHandler;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.http.HttpStatus;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.assertj.MvcTestResult;
import org.springframework.test.web.servlet.assertj.MockMvcTester;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

@WebMvcTest(RoomApiController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import(GlobalExceptionHandler.class)
class RoomApiWebMvcTest {
    private static final Instant CREATED_AT = Instant.parse("2026-04-09T00:00:00Z");
    private static final String ROOM_CODE = "ROOM01";
    private static final String HOST_ID = "host-1";
    private static final String HOST_TOKEN = "host-action-token";
    private static final String GUEST_ID = "guest-1";
    private static final String GUEST_TOKEN = "guest-action-token";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private CreateRoom createRoom;

    @MockitoBean
    private GetRoom getRoom;

    @MockitoBean
    private FindJoinableRooms findJoinableRooms;

    @MockitoBean
    private JoinRoom joinRoom;

    @MockitoBean
    private StartRoom startRoom;

    @MockitoBean
    private SelectDraftPosition selectDraftPosition;

    @MockitoBean
    private ClearDraftPosition clearDraftPosition;

    @Test
    void create는_room과_teamLeaderSession을_반환한다() throws Exception {
        Room room = waitingAuctionRoom();
        given(createRoom.create(org.mockito.ArgumentMatchers.any(), org.mockito.ArgumentMatchers.eq("호스트")))
            .willReturn(room);

        var result = mockMvcTester().perform(
            post("/api/v1/rooms")
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    """
                    {
                      "templateId": "11111111-1111-1111-1111-111111111111",
                      "hostNickname": "호스트"
                    }
                    """
                )
        );

        result.assertThat().hasStatus(HttpStatus.CREATED);
        assertThat(readBody(result, RoomSessionApiResponse.class))
            .satisfies(response -> {
                assertThat(response.resultType()).isEqualTo("SUCCESS");
                assertThat(response.success().room().code()).isEqualTo(ROOM_CODE);
                assertThat(response.success().room().status()).isEqualTo("WAITING");
                assertThat(response.success().teamLeaderSession().leaderId()).isEqualTo(HOST_ID);
                assertThat(response.success().teamLeaderSession().role()).isEqualTo("HOST");
                assertThat(response.success().teamLeaderSession().actionToken()).isEqualTo(HOST_TOKEN);
            });
    }

    @Test
    void create는_템플릿이_없으면_404를_반환한다() throws Exception {
        given(createRoom.create(org.mockito.ArgumentMatchers.any(), org.mockito.ArgumentMatchers.eq("호스트")))
            .willThrow(CoreException.of(RoomErrorType.ROOM_TEMPLATE_NOT_FOUND));

        var result = mockMvcTester().perform(
            post("/api/v1/rooms")
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    """
                    {
                      "templateId": "11111111-1111-1111-1111-111111111111",
                      "hostNickname": "호스트"
                    }
                    """
                )
        );

        result.assertThat().hasStatus(HttpStatus.NOT_FOUND);
        assertThat(readBody(result, VoidApiResponse.class))
            .satisfies(response -> {
                assertThat(response.resultType()).isEqualTo("ERROR");
                assertThat(response.error().code()).isEqualTo("ROOM_TEMPLATE_NOT_FOUND");
            });
    }

    @Test
    void get은_public_room_snapshot만_반환한다() throws Exception {
        given(getRoom.get(ROOM_CODE)).willReturn(waitingDraftRoom());

        var result = mockMvcTester().perform(get("/api/v1/rooms/{code}", ROOM_CODE));

        result.assertThat().hasStatusOk();
        JsonNode body = readTree(result);
        assertThat(body.at("/resultType").asText()).isEqualTo("SUCCESS");
        assertThat(body.at("/success/code").asText()).isEqualTo(ROOM_CODE);
        assertThat(body.at("/success/status").asText()).isEqualTo("WAITING");
        assertThat(body.at("/success/mode").asText()).isEqualTo("DRAFT");
        assertThat(body.at("/success/teamCount").asInt()).isEqualTo(2);
        assertThat(body.at("/success/teamSize").asInt()).isEqualTo(2);
        assertThat(body.at("/success/budget").isNull()).isTrue();
        assertThat(body.at("/success/draftOrderStrategy").asText()).isEqualTo("SNAKE");
        assertThat(body.at("/success/startReadiness").asText()).isEqualTo("WAITING_FOR_DRAFT_POSITIONS");
        assertThat(body.at("/success/teamLeaders/0/draftPosition").asInt()).isEqualTo(1);
        assertThat(body.at("/success/teamLeaders/1/draftPosition").isNull()).isTrue();
        assertThat(body.at("/success/players/0/name").asText()).isEqualTo("선수1");
        assertThat(body.at("/success/players/0/displayOrder").asInt()).isEqualTo(0);
        assertThat(body.at("/success/players/0/status").asText()).isEqualTo("AVAILABLE");
        assertThat(body.at("/success/players/1/name").asText()).isEqualTo("선수2");
        assertThat(body.at("/success/members").isArray()).isTrue();
        assertThat(body.at("/success/members")).hasSize(0);
        assertThat(body.at("/success/progress/currentTurnIndex").isNull()).isTrue();
        assertThat(body.at("/success/progress/currentRound").isNull()).isTrue();
        assertThat(body.at("/success/progress/currentLeaderId").isNull()).isTrue();
        assertThat(body.at("/success/progress/currentRoundLeaderIds").isNull()).isTrue();
        assertThat(result.getResponse().getContentAsString()).doesNotContain("teamLeaderSession");
    }

    @Test
    void get은_드래프트_진행중_스냅샷을_반환한다() throws Exception {
        given(getRoom.get(ROOM_CODE)).willReturn(inProgressDraftRoom());

        var result = mockMvcTester().perform(get("/api/v1/rooms/{code}", ROOM_CODE));

        result.assertThat().hasStatusOk();
        JsonNode body = readTree(result);
        assertThat(body.at("/success/status").asText()).isEqualTo("IN_PROGRESS");
        assertThat(body.at("/success/players/0/status").asText()).isEqualTo("ASSIGNED");
        assertThat(body.at("/success/players/1/status").asText()).isEqualTo("ASSIGNED");
        assertThat(body.at("/success/players/2/status").asText()).isEqualTo("AVAILABLE");
        assertThat(body.at("/success/members/0/teamLeaderId").asText()).isEqualTo(HOST_ID);
        assertThat(body.at("/success/members/0/playerName").asText()).isEqualTo("선수1");
        assertThat(body.at("/success/members/0/assignOrder").asInt()).isEqualTo(0);
        assertThat(body.at("/success/members/1/teamLeaderId").asText()).isEqualTo(GUEST_ID);
        assertThat(body.at("/success/members/1/playerName").asText()).isEqualTo("선수2");
        assertThat(body.at("/success/members/1/assignOrder").asInt()).isEqualTo(1);
        assertThat(body.at("/success/progress/currentTurnIndex").asInt()).isEqualTo(2);
        assertThat(body.at("/success/progress/currentRound").asInt()).isEqualTo(2);
        assertThat(body.at("/success/progress/currentLeaderId").asText()).isEqualTo(GUEST_ID);
        assertThat(body.at("/success/progress/currentRoundLeaderIds/0").asText()).isEqualTo(GUEST_ID);
        assertThat(body.at("/success/progress/currentRoundLeaderIds/1").asText()).isEqualTo(HOST_ID);
    }

    @Test
    void get은_완료된_방도_players_members_progress_null로_결과를_렌더링할_수_있다() throws Exception {
        given(getRoom.get(ROOM_CODE)).willReturn(completedDraftRoom());

        var result = mockMvcTester().perform(get("/api/v1/rooms/{code}", ROOM_CODE));

        result.assertThat().hasStatusOk();
        JsonNode body = readTree(result);
        assertThat(body.at("/success/status").asText()).isEqualTo("COMPLETED");
        assertThat(body.at("/success/players/0/status").asText()).isEqualTo("ASSIGNED");
        assertThat(body.at("/success/players/1/status").asText()).isEqualTo("ASSIGNED");
        assertThat(body.at("/success/members/0/teamLeaderId").asText()).isEqualTo(HOST_ID);
        assertThat(body.at("/success/members/1/teamLeaderId").asText()).isEqualTo(GUEST_ID);
        assertThat(body.at("/success/progress/currentTurnIndex").isNull()).isTrue();
        assertThat(body.at("/success/progress/currentRound").isNull()).isTrue();
        assertThat(body.at("/success/progress/currentLeaderId").isNull()).isTrue();
        assertThat(body.at("/success/progress/currentRoundLeaderIds").isNull()).isTrue();
    }

    @Test
    void get은_경매_진행중_스냅샷에서_currentRound를_currentAuctionRound로_반환한다() throws Exception {
        given(getRoom.get(ROOM_CODE)).willReturn(inProgressAuctionRoom());

        var result = mockMvcTester().perform(get("/api/v1/rooms/{code}", ROOM_CODE));

        result.assertThat().hasStatusOk();
        JsonNode body = readTree(result);
        assertThat(body.at("/success/status").asText()).isEqualTo("IN_PROGRESS");
        assertThat(body.at("/success/budget").asInt()).isEqualTo(300);
        assertThat(body.at("/success/draftOrderStrategy").isNull()).isTrue();
        assertThat(body.at("/success/players/0/status").asText()).isEqualTo("ASSIGNED");
        assertThat(body.at("/success/players/1/status").asText()).isEqualTo("AVAILABLE");
        assertThat(body.at("/success/members/0/teamLeaderId").asText()).isEqualTo(HOST_ID);
        assertThat(body.at("/success/members/0/playerName").asText()).isEqualTo("선수1");
        assertThat(body.at("/success/progress/currentTurnIndex").isNull()).isTrue();
        assertThat(body.at("/success/progress/currentRound").asInt()).isEqualTo(2);
        assertThat(body.at("/success/progress/currentLeaderId").isNull()).isTrue();
        assertThat(body.at("/success/progress/currentRoundLeaderIds").isNull()).isTrue();
    }

    @Test
    void get은_방이_없으면_404를_반환한다() throws Exception {
        given(getRoom.get(ROOM_CODE)).willThrow(CoreException.of(RoomErrorType.ROOM_NOT_FOUND));

        var result = mockMvcTester().perform(get("/api/v1/rooms/{code}", ROOM_CODE));

        result.assertThat().hasStatus(HttpStatus.NOT_FOUND);
        assertThat(readBody(result, VoidApiResponse.class))
            .satisfies(response -> {
                assertThat(response.resultType()).isEqualTo("ERROR");
                assertThat(response.error().code()).isEqualTo("ROOM_NOT_FOUND");
            });
    }

    @Test
    void list는_참여_가능한_room_목록을_반환한다() throws Exception {
        Room latest = waitingAuctionRoom("ROOM99", Instant.parse("2026-04-09T00:03:00Z"));
        Room older = waitingDraftRoom("ROOM01", Instant.parse("2026-04-09T00:01:00Z"));

        given(findJoinableRooms.list()).willReturn(List.of(latest, older));

        var result = mockMvcTester().perform(get("/api/v1/rooms"));

        result.assertThat().hasStatusOk();
        assertThat(readBody(result, JoinableRoomListApiResponse.class))
            .satisfies(response -> {
                assertThat(response.resultType()).isEqualTo("SUCCESS");
                assertThat(response.success()).hasSize(2);
                assertThat(response.success().getFirst().code()).isEqualTo("ROOM99");
                assertThat(response.success().getFirst().joinedLeaderCount()).isEqualTo(1);
                assertThat(response.success().getFirst().remainingSlotCount()).isEqualTo(1);
                assertThat(response.success().getFirst().startReadiness()).isEqualTo("WAITING_FOR_LEADERS");
            });
    }

    @Test
    void join은_room과_teamLeaderSession을_반환한다() throws Exception {
        Room room = joinedAuctionRoom();
        RoomTeamLeader guest = room.getLeaders().getLast();
        given(joinRoom.join(ROOM_CODE, "게스트")).willReturn(guest);
        given(getRoom.get(ROOM_CODE)).willReturn(room);

        var result = mockMvcTester().perform(
            post("/api/v1/rooms/{code}/join", ROOM_CODE)
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    """
                    {
                      "nickname": "게스트"
                    }
                    """
                )
        );

        result.assertThat().hasStatusOk();
        assertThat(readBody(result, RoomSessionApiResponse.class))
            .satisfies(response -> {
                assertThat(response.resultType()).isEqualTo("SUCCESS");
                assertThat(response.success().room().code()).isEqualTo(ROOM_CODE);
                assertThat(response.success().room().teamLeaders()).hasSize(2);
                assertThat(response.success().teamLeaderSession().leaderId()).isEqualTo(GUEST_ID);
                assertThat(response.success().teamLeaderSession().role()).isEqualTo("LEADER");
                assertThat(response.success().teamLeaderSession().actionToken()).isEqualTo(GUEST_TOKEN);
            });
    }

    @Test
    void start는_header가_있으면_성공한다() throws Exception {
        Room room = startedAuctionRoom();
        doNothing().when(startRoom).start(ROOM_CODE, HOST_TOKEN);
        given(getRoom.get(ROOM_CODE)).willReturn(room);

        var result = mockMvcTester().perform(
            post("/api/v1/rooms/{code}/start", ROOM_CODE)
                .header("X-Room-Action-Token", HOST_TOKEN)
        );

        result.assertThat().hasStatusOk();
        assertThat(readBody(result, RoomResponseApiResponse.class))
            .satisfies(response -> {
                assertThat(response.resultType()).isEqualTo("SUCCESS");
                assertThat(response.success().status()).isEqualTo("IN_PROGRESS");
            });
    }

    @Test
    void start는_header가_없으면_401을_반환한다() throws Exception {
        doThrow(CoreException.of(RoomErrorType.ROOM_ACTION_TOKEN_REQUIRED))
            .when(startRoom)
            .start(ROOM_CODE, null);

        var result = mockMvcTester().perform(post("/api/v1/rooms/{code}/start", ROOM_CODE));

        result.assertThat().hasStatus(HttpStatus.UNAUTHORIZED);
        assertThat(readBody(result, VoidApiResponse.class))
            .satisfies(response -> {
                assertThat(response.resultType()).isEqualTo("ERROR");
                assertThat(response.error().code()).isEqualTo("ROOM_ACTION_TOKEN_REQUIRED");
            });
    }

    @Test
    void selectDraftPosition은_header가_있으면_성공한다() throws Exception {
        Room room = waitingDraftRoom();
        room.selectDraftPosition(GUEST_ID, 2);
        doNothing().when(selectDraftPosition).select(ROOM_CODE, GUEST_TOKEN, 2);
        given(getRoom.get(ROOM_CODE)).willReturn(room);

        var result = mockMvcTester().perform(
            put("/api/v1/rooms/{code}/draft-position", ROOM_CODE)
                .header("X-Room-Action-Token", GUEST_TOKEN)
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    """
                    {
                      "draftPosition": 2
                    }
                    """
                )
        );

        result.assertThat().hasStatusOk();
        assertThat(readBody(result, RoomResponseApiResponse.class))
            .satisfies(response -> {
                assertThat(response.success().startReadiness()).isEqualTo("STARTABLE");
                assertThat(response.success().teamLeaders()).extracting(TeamLeaderResponse::draftPosition)
                    .containsExactly(1, 2);
            });
    }

    @Test
    void clearDraftPosition은_header가_있으면_성공한다() throws Exception {
        Room room = waitingDraftRoom();
        doNothing().when(clearDraftPosition).clear(ROOM_CODE, HOST_TOKEN);
        given(getRoom.get(ROOM_CODE)).willReturn(room);

        var result = mockMvcTester().perform(
            delete("/api/v1/rooms/{code}/draft-position", ROOM_CODE)
                .header("X-Room-Action-Token", HOST_TOKEN)
        );

        result.assertThat().hasStatusOk();
        assertThat(readBody(result, RoomResponseApiResponse.class))
            .satisfies(response -> {
                assertThat(response.success().startReadiness()).isEqualTo("WAITING_FOR_DRAFT_POSITIONS");
                assertThat(response.success().teamLeaders()).extracting(TeamLeaderResponse::draftPosition)
                    .containsExactly(1, null);
            });
    }

    @Test
    void selectDraftPosition은_header가_없으면_401을_반환한다() throws Exception {
        doThrow(CoreException.of(RoomErrorType.ROOM_ACTION_TOKEN_REQUIRED))
            .when(selectDraftPosition)
            .select(ROOM_CODE, null, 2);

        var result = mockMvcTester().perform(
            put("/api/v1/rooms/{code}/draft-position", ROOM_CODE)
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    """
                    {
                      "draftPosition": 2
                    }
                    """
                )
        );

        result.assertThat().hasStatus(HttpStatus.UNAUTHORIZED);
        assertThat(readBody(result, VoidApiResponse.class))
            .satisfies(response -> {
                assertThat(response.resultType()).isEqualTo("ERROR");
                assertThat(response.error().code()).isEqualTo("ROOM_ACTION_TOKEN_REQUIRED");
            });
    }

    private MockMvcTester mockMvcTester() {
        return MockMvcTester.create(mockMvc);
    }

    private <T> T readBody(MvcTestResult result, Class<T> bodyType) throws Exception {
        return objectMapper.readValue(result.getResponse().getContentAsString(), bodyType);
    }

    private JsonNode readTree(MvcTestResult result) throws Exception {
        return objectMapper.readTree(result.getResponse().getContentAsString());
    }

    private Room waitingAuctionRoom() {
        return waitingAuctionRoom(ROOM_CODE, CREATED_AT);
    }

    private Room waitingAuctionRoom(String code, Instant createdAt) {
        return Room.createFromTemplate(
            code,
            new TeamLeaderId(HOST_ID),
            "호스트",
            HOST_TOKEN,
            new RoomTemplateSpec(
                RoomTemplateSpec.Mode.AUCTION,
                2,
                2,
                300,
                null,
                List.of(
                    new RoomTemplateSpec.Player(new RoomPlayerId(0), "선수1", 0),
                    new RoomTemplateSpec.Player(new RoomPlayerId(1), "선수2", 1)
                )
            ),
            createdAt
        );
    }

    private Room joinedAuctionRoom() {
        Room room = waitingAuctionRoom();
        room.join(new TeamLeaderId(GUEST_ID), "게스트", GUEST_TOKEN);
        return room;
    }

    private Room waitingDraftRoom() {
        return waitingDraftRoom(ROOM_CODE, CREATED_AT);
    }

    private Room waitingDraftRoom(String code, Instant createdAt) {
        Room room =
            Room.createFromTemplate(
                code,
                new TeamLeaderId(HOST_ID),
                "호스트",
                HOST_TOKEN,
                new RoomTemplateSpec(
                    RoomTemplateSpec.Mode.DRAFT,
                    2,
                    2,
                    null,
                    RoomTemplateSpec.DraftOrderStrategy.SNAKE,
                    List.of(
                        new RoomTemplateSpec.Player(new RoomPlayerId(0), "선수1", 0),
                        new RoomTemplateSpec.Player(new RoomPlayerId(1), "선수2", 1)
                    )
                ),
                createdAt
            );
        room.join(new TeamLeaderId(GUEST_ID), "게스트", GUEST_TOKEN);
        room.selectDraftPosition(new TeamLeaderId(HOST_ID), 1);
        return room;
    }

    private Room startedAuctionRoom() {
        Room room = joinedAuctionRoom();
        room.start(new TeamLeaderId(HOST_ID));
        return room;
    }

    private Room inProgressAuctionRoom() {
        Room room =
            Room.createFromTemplate(
                ROOM_CODE,
                new TeamLeaderId(HOST_ID),
                "호스트",
                HOST_TOKEN,
                new RoomTemplateSpec(
                    RoomTemplateSpec.Mode.AUCTION,
                    2,
                    3,
                    300,
                    null,
                    List.of(
                        new RoomTemplateSpec.Player(new RoomPlayerId(0), "선수1", 0),
                        new RoomTemplateSpec.Player(new RoomPlayerId(1), "선수2", 1),
                        new RoomTemplateSpec.Player(new RoomPlayerId(2), "선수3", 2),
                        new RoomTemplateSpec.Player(new RoomPlayerId(3), "선수4", 3)
                    )
                ),
                CREATED_AT
            );
        room.join(new TeamLeaderId(GUEST_ID), "게스트", GUEST_TOKEN);
        room.start(new TeamLeaderId(HOST_ID));
        room.placeBid(new TeamLeaderId(HOST_ID), 100);
        room.settleAuction();
        return room;
    }

    private Room inProgressDraftRoom() {
        Room room =
            Room.createFromTemplate(
                ROOM_CODE,
                new TeamLeaderId(HOST_ID),
                "호스트",
                HOST_TOKEN,
                new RoomTemplateSpec(
                    RoomTemplateSpec.Mode.DRAFT,
                    2,
                    3,
                    null,
                    RoomTemplateSpec.DraftOrderStrategy.SNAKE,
                    List.of(
                        new RoomTemplateSpec.Player(new RoomPlayerId(0), "선수1", 0),
                        new RoomTemplateSpec.Player(new RoomPlayerId(1), "선수2", 1),
                        new RoomTemplateSpec.Player(new RoomPlayerId(2), "선수3", 2),
                        new RoomTemplateSpec.Player(new RoomPlayerId(3), "선수4", 3)
                    )
                ),
                CREATED_AT
            );
        room.join(new TeamLeaderId(GUEST_ID), "게스트", GUEST_TOKEN);
        room.selectDraftPosition(new TeamLeaderId(HOST_ID), 1);
        room.selectDraftPosition(new TeamLeaderId(GUEST_ID), 2);
        room.start(new TeamLeaderId(HOST_ID));
        room.pick(new TeamLeaderId(HOST_ID), "선수1");
        room.pick(new TeamLeaderId(GUEST_ID), "선수2");
        return room;
    }

    private Room completedDraftRoom() {
        Room room = waitingDraftRoom();
        room.selectDraftPosition(new TeamLeaderId(GUEST_ID), 2);
        room.start(new TeamLeaderId(HOST_ID));
        room.pick(new TeamLeaderId(HOST_ID), "선수1");
        room.pick(new TeamLeaderId(GUEST_ID), "선수2");
        return room;
    }

    private record RoomSessionApiResponse(
        String resultType,
        RoomSessionResponse success,
        ErrorMessage error
    ) {
    }

    private record RoomResponseApiResponse(
        String resultType,
        RoomResponse success,
        ErrorMessage error
    ) {
    }

    private record VoidApiResponse(
        String resultType,
        Void success,
        ErrorMessage error
    ) {
    }

    private record JoinableRoomListApiResponse(
        String resultType,
        List<JoinableRoomResponse> success,
        ErrorMessage error
    ) {
    }
}
