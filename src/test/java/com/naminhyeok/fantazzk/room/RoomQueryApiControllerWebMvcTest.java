package com.naminhyeok.fantazzk.room;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

import com.naminhyeok.fantazzk.CoreException;
import com.naminhyeok.fantazzk.ErrorMessage;
import com.naminhyeok.fantazzk.GlobalExceptionHandler;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.assertj.MockMvcTester;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

@WebMvcTest(RoomQueryApiController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import(GlobalExceptionHandler.class)
class RoomQueryApiControllerWebMvcTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private GetRoomDetails getRoomDetails;

    @MockitoBean
    private FindJoinableRooms findJoinableRooms;

    @Test
    void get은_public_room_snapshot만_반환한다() throws Exception {
        given(getRoomDetails.get(RoomApiTestFixtures.ROOM_CODE)).willReturn(RoomDetails.from(RoomApiTestFixtures.waitingDraftRoom()));

        var result = mockMvcTester().perform(get("/api/v1/rooms/{code}", RoomApiTestFixtures.ROOM_CODE));

        result.assertThat().hasStatusOk();
        JsonNode body = objectMapper.readTree(result.getResponse().getContentAsString());
        assertThat(body.at("/resultType").asText()).isEqualTo("SUCCESS");
        assertThat(body.at("/success/code").asText()).isEqualTo(RoomApiTestFixtures.ROOM_CODE);
        assertThat(body.at("/success/status").asText()).isEqualTo("WAITING");
        assertThat(body.at("/success/mode").asText()).isEqualTo("DRAFT");
        assertThat(body.at("/success/startReadiness").asText()).isEqualTo("WAITING_FOR_DRAFT_POSITIONS");
        assertThat(result.getResponse().getContentAsString()).doesNotContain("teamLeaderSession");
    }

    @Test
    void get은_방이_없으면_404를_반환한다() throws Exception {
        given(getRoomDetails.get(RoomApiTestFixtures.ROOM_CODE)).willThrow(CoreException.of(RoomErrorType.ROOM_NOT_FOUND));

        var result = mockMvcTester().perform(get("/api/v1/rooms/{code}", RoomApiTestFixtures.ROOM_CODE));

        result.assertThat().hasStatus(HttpStatus.NOT_FOUND);
        assertThat(readBody(result, VoidApiResponse.class).error().code()).isEqualTo("ROOM_NOT_FOUND");
    }

    @Test
    void list는_참여_가능한_room_목록을_반환한다() throws Exception {
        Room latest = RoomApiTestFixtures.waitingAuctionRoom("ROOM99", Instant.parse("2026-04-09T00:03:00Z"));
        Room older = RoomApiTestFixtures.waitingDraftRoom("ROOM01", Instant.parse("2026-04-09T00:01:00Z"));
        given(findJoinableRooms.list()).willReturn(List.of(
            JoinableRoomResponse.from(latest),
            JoinableRoomResponse.from(older)
        ));

        var result = mockMvcTester().perform(get("/api/v1/rooms"));

        result.assertThat().hasStatusOk();
        JoinableRoomListApiResponse body = readBody(result, JoinableRoomListApiResponse.class);
        assertThat(body.resultType()).isEqualTo("SUCCESS");
        assertThat(body.success()).hasSize(2);
        assertThat(body.success().getFirst().code()).isEqualTo("ROOM99");
        assertThat(body.success().getFirst().mode()).isEqualTo("AUCTION");
    }

    private MockMvcTester mockMvcTester() {
        return MockMvcTester.create(mockMvc);
    }

    private <T> T readBody(org.springframework.test.web.servlet.assertj.MvcTestResult result, Class<T> bodyType) throws Exception {
        return objectMapper.readValue(result.getResponse().getContentAsString(), bodyType);
    }

    private record VoidApiResponse(String resultType, Void success, ErrorMessage error) {}

    private record JoinableRoomListApiResponse(
        String resultType,
        List<JoinableRoomResponse> success,
        ErrorMessage error
    ) {}
}
