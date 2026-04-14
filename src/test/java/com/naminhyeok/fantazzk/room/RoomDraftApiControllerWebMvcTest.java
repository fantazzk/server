package com.naminhyeok.fantazzk.room;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doThrow;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;

import com.naminhyeok.fantazzk.CoreException;
import com.naminhyeok.fantazzk.ErrorMessage;
import com.naminhyeok.fantazzk.GlobalExceptionHandler;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.assertj.MockMvcTester;
import tools.jackson.databind.ObjectMapper;

@WebMvcTest(RoomDraftApiController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import(GlobalExceptionHandler.class)
class RoomDraftApiControllerWebMvcTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private PickDraft pickDraft;

    @MockitoBean
    private SelectDraftPosition selectDraftPosition;

    @MockitoBean
    private ClearDraftPosition clearDraftPosition;

    @Test
    void pickDraft는_header가_있으면_최신_room_snapshot을_반환한다() throws Exception {
        given(pickDraft.pick(RoomApiTestFixtures.ROOM_CODE, RoomApiTestFixtures.GUEST_TOKEN, "선수3"))
            .willReturn(RoomApiTestFixtures.inProgressDraftRoom());

        var result = mockMvcTester().perform(
            post("/api/v1/rooms/{code}/draft-picks", RoomApiTestFixtures.ROOM_CODE)
                .header("X-Room-Action-Token", RoomApiTestFixtures.GUEST_TOKEN)
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    """
                    {
                      "playerName": "선수3"
                    }
                    """
                )
        );

        result.assertThat().hasStatusOk();
        RoomResponseApiResponse body = readBody(result, RoomResponseApiResponse.class);
        assertThat(body.resultType()).isEqualTo("SUCCESS");
        assertThat(body.success().status()).isEqualTo("IN_PROGRESS");
        assertThat(body.success().code()).isEqualTo(RoomApiTestFixtures.ROOM_CODE);
    }

    @Test
    void pickDraft는_header가_없으면_401을_반환한다() throws Exception {
        doThrow(CoreException.of(RoomErrorType.ROOM_ACTION_TOKEN_REQUIRED))
            .when(pickDraft)
            .pick(RoomApiTestFixtures.ROOM_CODE, null, "선수3");

        var result = mockMvcTester().perform(
            post("/api/v1/rooms/{code}/draft-picks", RoomApiTestFixtures.ROOM_CODE)
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    """
                    {
                      "playerName": "선수3"
                    }
                    """
                )
        );

        result.assertThat().hasStatus(HttpStatus.UNAUTHORIZED);
        assertThat(readBody(result, VoidApiResponse.class).error().code()).isEqualTo("ROOM_ACTION_TOKEN_REQUIRED");
    }

    @Test
    void pickDraft는_optimistic_lock_conflict를_409로_반환한다() throws Exception {
        doThrow(CoreException.of(RoomErrorType.ROOM_CONCURRENT_MODIFICATION))
            .when(pickDraft)
            .pick(RoomApiTestFixtures.ROOM_CODE, RoomApiTestFixtures.GUEST_TOKEN, "선수3");

        var result = mockMvcTester().perform(
            post("/api/v1/rooms/{code}/draft-picks", RoomApiTestFixtures.ROOM_CODE)
                .header("X-Room-Action-Token", RoomApiTestFixtures.GUEST_TOKEN)
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    """
                    {
                      "playerName": "선수3"
                    }
                    """
                )
        );

        result.assertThat().hasStatus(HttpStatus.CONFLICT);
        assertThat(readBody(result, VoidApiResponse.class).error().code()).isEqualTo("ROOM_CONCURRENT_MODIFICATION");
    }

    @Test
    void selectDraftPosition은_header가_있으면_성공한다() throws Exception {
        Room room = RoomApiTestFixtures.waitingDraftRoom();
        room.selectDraftPosition(new TeamLeaderId(RoomApiTestFixtures.GUEST_ID), 2);
        given(selectDraftPosition.select(RoomApiTestFixtures.ROOM_CODE, RoomApiTestFixtures.GUEST_TOKEN, 2))
            .willReturn(room);

        var result = mockMvcTester().perform(
            put("/api/v1/rooms/{code}/draft-position", RoomApiTestFixtures.ROOM_CODE)
                .header("X-Room-Action-Token", RoomApiTestFixtures.GUEST_TOKEN)
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
        RoomResponseApiResponse body = readBody(result, RoomResponseApiResponse.class);
        assertThat(body.success().startReadiness()).isEqualTo("STARTABLE");
        assertThat(body.success().code()).isEqualTo(RoomApiTestFixtures.ROOM_CODE);
    }

    @Test
    void clearDraftPosition은_header가_있으면_성공한다() throws Exception {
        Room room = RoomApiTestFixtures.waitingDraftRoom();
        room.clearDraftPosition(new TeamLeaderId(RoomApiTestFixtures.HOST_ID));
        given(clearDraftPosition.clear(RoomApiTestFixtures.ROOM_CODE, RoomApiTestFixtures.HOST_TOKEN))
            .willReturn(room);

        var result = mockMvcTester().perform(
            delete("/api/v1/rooms/{code}/draft-position", RoomApiTestFixtures.ROOM_CODE)
                .header("X-Room-Action-Token", RoomApiTestFixtures.HOST_TOKEN)
        );

        result.assertThat().hasStatusOk();
        RoomResponseApiResponse body = readBody(result, RoomResponseApiResponse.class);
        assertThat(body.success().startReadiness()).isEqualTo("WAITING_FOR_DRAFT_POSITIONS");
        assertThat(body.success().code()).isEqualTo(RoomApiTestFixtures.ROOM_CODE);
    }

    @Test
    void selectDraftPosition은_header가_없으면_401을_반환한다() throws Exception {
        doThrow(CoreException.of(RoomErrorType.ROOM_ACTION_TOKEN_REQUIRED))
            .when(selectDraftPosition)
            .select(RoomApiTestFixtures.ROOM_CODE, null, 2);

        var result = mockMvcTester().perform(
            put("/api/v1/rooms/{code}/draft-position", RoomApiTestFixtures.ROOM_CODE)
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
        assertThat(readBody(result, VoidApiResponse.class).error().code()).isEqualTo("ROOM_ACTION_TOKEN_REQUIRED");
    }

    private MockMvcTester mockMvcTester() {
        return MockMvcTester.create(mockMvc);
    }

    private <T> T readBody(org.springframework.test.web.servlet.assertj.MvcTestResult result, Class<T> bodyType) throws Exception {
        return objectMapper.readValue(result.getResponse().getContentAsString(), bodyType);
    }

    private record RoomResponseApiResponse(String resultType, RoomResponse success, ErrorMessage error) {}

    private record VoidApiResponse(String resultType, Void success, ErrorMessage error) {}
}
