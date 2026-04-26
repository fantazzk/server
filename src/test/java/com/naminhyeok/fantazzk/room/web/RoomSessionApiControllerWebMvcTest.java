package com.naminhyeok.fantazzk.room.web;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

import com.naminhyeok.fantazzk.CoreException;
import com.naminhyeok.fantazzk.ErrorMessage;
import com.naminhyeok.fantazzk.GlobalExceptionHandler;
import com.naminhyeok.fantazzk.room.application.CreateRoom;
import com.naminhyeok.fantazzk.room.application.JoinRoom;
import com.naminhyeok.fantazzk.room.application.RoomSessionResult;
import com.naminhyeok.fantazzk.room.domain.Room;
import com.naminhyeok.fantazzk.room.domain.RoomErrorType;
import com.naminhyeok.fantazzk.room.domain.RoomTeamLeader;
import com.naminhyeok.fantazzk.room.support.RoomApiTestFixtures;
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
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

@WebMvcTest(RoomSessionApiController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import(GlobalExceptionHandler.class)
class RoomSessionApiControllerWebMvcTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private CreateRoom createRoom;

    @MockitoBean
    private JoinRoom joinRoom;

    @Test
    void 방_생성_API는_방과_호스트_세션을_반환한다() throws Exception {
        Room room = RoomApiTestFixtures.waitingAuctionRoom();
        RoomTeamLeader host = room.getLeaders().getFirst();
        given(createRoom.create(any(), eq("호스트")))
            .willReturn(new RoomSessionResult(room, host));

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
        JsonNode body = readJsonBody(result);
        assertThat(body.at("/resultType").asText()).isEqualTo("SUCCESS");
        assertThat(body.at("/success/room/roomCode").asText()).isEqualTo(RoomApiTestFixtures.ROOM_CODE);
        assertThat(body.at("/success/room/status").asText()).isEqualTo("WAITING");
        assertThat(body.at("/success/room/leaders").size()).isEqualTo(1);
        assertThat(body.at("/success/room/playerPool").size()).isEqualTo(2);
        assertThat(body.at("/success/teamLeaderSession/leaderId").asText()).isEqualTo(RoomApiTestFixtures.HOST_ID);
        assertThat(body.at("/success/teamLeaderSession/role").asText()).isEqualTo("HOST");
        assertThat(body.at("/success/teamLeaderSession/actionToken").asText()).isEqualTo(RoomApiTestFixtures.HOST_TOKEN);
    }

    @Test
    void 방_생성_API는_템플릿이_없으면_404를_반환한다() throws Exception {
        given(createRoom.create(any(), eq("호스트")))
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
        assertThat(readBody(result, VoidApiResponse.class).error().code()).isEqualTo("ROOM_TEMPLATE_NOT_FOUND");
    }

    @Test
    void 방_참가_API는_방과_참가자_세션을_반환한다() throws Exception {
        Room room = RoomApiTestFixtures.joinedAuctionRoom();
        RoomTeamLeader guest = room.getLeaders().getLast();
        given(joinRoom.join(RoomApiTestFixtures.ROOM_CODE, "게스트"))
            .willReturn(new RoomSessionResult(room, guest));

        var result = mockMvcTester().perform(
            post("/api/v1/rooms/{code}/join", RoomApiTestFixtures.ROOM_CODE)
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
        JsonNode body = readJsonBody(result);
        assertThat(body.at("/resultType").asText()).isEqualTo("SUCCESS");
        assertThat(body.at("/success/room/roomCode").asText()).isEqualTo(RoomApiTestFixtures.ROOM_CODE);
        assertThat(body.at("/success/room/leaders").size()).isEqualTo(2);
        assertThat(body.at("/success/room/playerPool").size()).isEqualTo(2);
        assertThat(body.at("/success/teamLeaderSession/leaderId").asText()).isEqualTo(RoomApiTestFixtures.GUEST_ID);
        assertThat(body.at("/success/teamLeaderSession/role").asText()).isEqualTo("LEADER");
        assertThat(body.at("/success/teamLeaderSession/actionToken").asText()).isEqualTo(RoomApiTestFixtures.GUEST_TOKEN);
    }

    @Test
    void 방_참가_API는_닉네임이_중복되면_409를_반환한다() throws Exception {
        given(joinRoom.join(RoomApiTestFixtures.ROOM_CODE, "게스트"))
            .willThrow(CoreException.of(RoomErrorType.ROOM_NICKNAME_ALREADY_TAKEN));

        var result = mockMvcTester().perform(
            post("/api/v1/rooms/{code}/join", RoomApiTestFixtures.ROOM_CODE)
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    """
                    {
                      "nickname": "게스트"
                    }
                    """
                )
        );

        result.assertThat().hasStatus(HttpStatus.CONFLICT);
        assertThat(readBody(result, VoidApiResponse.class).error().code()).isEqualTo("ROOM_NICKNAME_ALREADY_TAKEN");
    }

    private MockMvcTester mockMvcTester() {
        return MockMvcTester.create(mockMvc);
    }

    private <T> T readBody(org.springframework.test.web.servlet.assertj.MvcTestResult result, Class<T> bodyType) throws Exception {
        return objectMapper.readValue(result.getResponse().getContentAsString(), bodyType);
    }

    private JsonNode readJsonBody(org.springframework.test.web.servlet.assertj.MvcTestResult result) throws Exception {
        return objectMapper.readTree(result.getResponse().getContentAsString());
    }

    private record VoidApiResponse(String resultType, Void success, ErrorMessage error) {}
}
