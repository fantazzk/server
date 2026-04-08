package com.naminhyeok.fantazzk.room;

import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.assertj.core.api.Assertions.assertThat;

import com.naminhyeok.fantazzk.CoreException;
import com.naminhyeok.fantazzk.ErrorMessage;
import com.naminhyeok.fantazzk.GlobalExceptionHandler;
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
import tools.jackson.databind.ObjectMapper;

@WebMvcTest(RoomApiController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import(GlobalExceptionHandler.class)
class RoomApiWebMvcTest {
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
    private JoinRoom joinRoom;

    @MockitoBean
    private StartRoom startRoom;

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
        given(getRoom.get(ROOM_CODE)).willReturn(waitingAuctionRoom());

        var result = mockMvcTester().perform(get("/api/v1/rooms/{code}", ROOM_CODE));

        result.assertThat().hasStatusOk();
        assertThat(readBody(result, RoomResponseApiResponse.class))
            .satisfies(response -> {
                assertThat(response.resultType()).isEqualTo("SUCCESS");
                assertThat(response.success().code()).isEqualTo(ROOM_CODE);
                assertThat(response.success().status()).isEqualTo("WAITING");
            });
        assertThat(result.getResponse().getContentAsString()).doesNotContain("teamLeaderSession");
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

    private MockMvcTester mockMvcTester() {
        return MockMvcTester.create(mockMvc);
    }

    private <T> T readBody(MvcTestResult result, Class<T> bodyType) throws Exception {
        return objectMapper.readValue(result.getResponse().getContentAsString(), bodyType);
    }

    private Room waitingAuctionRoom() {
        return Room.createFromTemplate(
            ROOM_CODE,
            HOST_ID,
            "호스트",
            HOST_TOKEN,
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
            )
        );
    }

    private Room joinedAuctionRoom() {
        Room room = waitingAuctionRoom();
        room.join(GUEST_ID, "게스트", GUEST_TOKEN);
        return room;
    }

    private Room startedAuctionRoom() {
        Room room = joinedAuctionRoom();
        room.start(HOST_ID);
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
}
