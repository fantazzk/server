package com.naminhyeok.fantazzk.room.web;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

import com.naminhyeok.fantazzk.CoreException;
import com.naminhyeok.fantazzk.ErrorMessage;
import com.naminhyeok.fantazzk.GlobalExceptionHandler;
import com.naminhyeok.fantazzk.room.domain.Room;
import com.naminhyeok.fantazzk.room.domain.RoomErrorType;
import com.naminhyeok.fantazzk.room.query.FindJoinableRooms;
import com.naminhyeok.fantazzk.room.query.GetRoom;
import com.naminhyeok.fantazzk.room.query.JoinableRoomResponse;
import com.naminhyeok.fantazzk.room.web.RoomQueryApiController;
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
import com.naminhyeok.fantazzk.room.support.RoomApiTestFixtures;

@WebMvcTest(RoomQueryApiController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import(GlobalExceptionHandler.class)
class RoomQueryApiControllerWebMvcTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private GetRoom getRoom;

    @MockitoBean
    private FindJoinableRooms findJoinableRooms;

    @Test
    void 방_조회_API는_로비_화면_계약만_반환한다() throws Exception {
        given(getRoom.get(RoomApiTestFixtures.ROOM_CODE)).willReturn(RoomApiTestFixtures.waitingDraftRoom());

        var result = mockMvcTester().perform(get("/api/v1/rooms/{code}", RoomApiTestFixtures.ROOM_CODE));

        result.assertThat().hasStatusOk();
        JsonNode body = objectMapper.readTree(result.getResponse().getContentAsString());
        assertThat(body.at("/resultType").asText()).isEqualTo("SUCCESS");
        assertThat(body.at("/success/roomCode").asText()).isEqualTo(RoomApiTestFixtures.ROOM_CODE);
        assertThat(body.at("/success/status").asText()).isEqualTo("WAITING");
        assertThat(body.at("/success/mode").asText()).isEqualTo("DRAFT");
        assertThat(body.at("/success/startReadiness").asText()).isEqualTo("WAITING_FOR_DRAFT_POSITIONS");
        assertThat(body.at("/success/leaders/0/nickname").asText()).isEqualTo("호스트");
        assertThat(body.at("/success/playerPool/0/name").asText()).isEqualTo("선수1");
        assertThat(body.at("/success/draftOrder/slots/0/leaderId").asText()).isEqualTo(RoomApiTestFixtures.HOST_ID);
        assertThat(body.at("/success/code").isMissingNode()).isTrue();
        assertThat(body.at("/success/teamLeaders").isMissingNode()).isTrue();
        assertThat(body.at("/success/players").isMissingNode()).isTrue();
        assertThat(body.at("/success/members").isMissingNode()).isTrue();
        assertThat(body.at("/success/auctionProgress").isMissingNode()).isTrue();
        assertThat(body.at("/success/draftProgress").isMissingNode()).isTrue();
        assertThat(result.getResponse().getContentAsString()).doesNotContain("teamLeaderSession");
    }

    @Test
    void 시작된_방_조회_API는_게임_ID만_연결하고_진행_상태는_분리한다() throws Exception {
        given(getRoom.get(RoomApiTestFixtures.ROOM_CODE)).willReturn(RoomApiTestFixtures.startedAuctionRoom());

        var result = mockMvcTester().perform(get("/api/v1/rooms/{code}", RoomApiTestFixtures.ROOM_CODE));

        result.assertThat().hasStatusOk();
        JsonNode body = objectMapper.readTree(result.getResponse().getContentAsString());
        assertThat(body.at("/success/status").asText()).isEqualTo("STARTED");
        assertThat(body.at("/success/startedGameId").asText()).isEqualTo(RoomApiTestFixtures.GAME_ID);
        assertThat(body.at("/success/auctionProgress").isMissingNode()).isTrue();
        assertThat(body.at("/success/draftProgress").isMissingNode()).isTrue();
        assertThat(body.at("/success/members").isMissingNode()).isTrue();
    }

    @Test
    void 방_조회_API는_없는_방을_404로_반환한다() throws Exception {
        given(getRoom.get(RoomApiTestFixtures.ROOM_CODE)).willThrow(CoreException.of(RoomErrorType.ROOM_NOT_FOUND));

        var result = mockMvcTester().perform(get("/api/v1/rooms/{code}", RoomApiTestFixtures.ROOM_CODE));

        result.assertThat().hasStatus(HttpStatus.NOT_FOUND);
        assertThat(readBody(result, VoidApiResponse.class).error().code()).isEqualTo("ROOM_NOT_FOUND");
    }

    @Test
    void 방_목록_API는_참여_가능한_방만_반환한다() throws Exception {
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
