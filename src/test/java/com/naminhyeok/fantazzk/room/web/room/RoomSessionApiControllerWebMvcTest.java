package com.naminhyeok.fantazzk.room.web.room;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

import com.naminhyeok.fantazzk.CoreException;
import com.naminhyeok.fantazzk.ErrorDescriptor;
import com.naminhyeok.fantazzk.ErrorMessage;
import com.naminhyeok.fantazzk.GlobalExceptionHandler;
import com.naminhyeok.fantazzk.room.RoomApiTestFixtures;
import com.naminhyeok.fantazzk.room.RoomSessionApi;
import com.naminhyeok.fantazzk.room.RoomSessionView;
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
import org.slf4j.event.Level;
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
    private RoomSessionApi roomSessionApi;

    @Test
    void create는_room과_teamLeaderSession을_반환한다() throws Exception {
        given(roomSessionApi.create(any(), eq("호스트")))
            .willReturn(RoomApiTestFixtures.createdAuctionRoomSessionView());

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
        RoomSessionApiResponse body = readBody(result, RoomSessionApiResponse.class);
        assertThat(body.resultType()).isEqualTo("SUCCESS");
        assertThat(body.success().room().code()).isEqualTo(RoomApiTestFixtures.ROOM_CODE);
        assertThat(body.success().room().status()).isEqualTo("WAITING");
        assertThat(body.success().teamLeaderSession().leaderId()).isEqualTo(RoomApiTestFixtures.HOST_ID);
        assertThat(body.success().teamLeaderSession().role()).isEqualTo("HOST");
        assertThat(body.success().teamLeaderSession().actionToken()).isEqualTo(RoomApiTestFixtures.HOST_TOKEN);
    }

    @Test
    void create는_템플릿이_없으면_404를_반환한다() throws Exception {
        given(roomSessionApi.create(any(), eq("호스트")))
            .willThrow(coreException(HttpStatus.NOT_FOUND, "ROOM_TEMPLATE_NOT_FOUND"));

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
    void join은_room과_teamLeaderSession을_반환한다() throws Exception {
        given(roomSessionApi.join(RoomApiTestFixtures.ROOM_CODE, "게스트"))
            .willReturn(RoomApiTestFixtures.joinedAuctionRoomSessionView());

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
        RoomSessionApiResponse body = readBody(result, RoomSessionApiResponse.class);
        assertThat(body.resultType()).isEqualTo("SUCCESS");
        assertThat(body.success().room().code()).isEqualTo(RoomApiTestFixtures.ROOM_CODE);
        assertThat(body.success().teamLeaderSession().leaderId()).isEqualTo(RoomApiTestFixtures.GUEST_ID);
        assertThat(body.success().teamLeaderSession().role()).isEqualTo("LEADER");
        assertThat(body.success().teamLeaderSession().actionToken()).isEqualTo(RoomApiTestFixtures.GUEST_TOKEN);
    }

    @Test
    void join은_닉네임이_중복되면_409를_반환한다() throws Exception {
        given(roomSessionApi.join(RoomApiTestFixtures.ROOM_CODE, "게스트"))
            .willThrow(coreException(HttpStatus.CONFLICT, "ROOM_NICKNAME_ALREADY_TAKEN"));

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

    private CoreException coreException(HttpStatus status, String code) {
        return CoreException.of(new StubErrorDescriptor(status, code, code));
    }

    private record RoomSessionApiResponse(String resultType, RoomSessionView success, ErrorMessage error) {}

    private record VoidApiResponse(String resultType, Void success, ErrorMessage error) {}

    private record StubErrorDescriptor(HttpStatus status, String code, String message) implements ErrorDescriptor {
        @Override
        public HttpStatus getStatus() {
            return status;
        }

        @Override
        public String getCode() {
            return code;
        }

        @Override
        public String getMessage() {
            return message;
        }

        @Override
        public Level getLogLevel() {
            return Level.INFO;
        }
    }
}
