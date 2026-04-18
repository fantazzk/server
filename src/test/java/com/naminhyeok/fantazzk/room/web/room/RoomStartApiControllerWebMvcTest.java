package com.naminhyeok.fantazzk.room.web.room;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doThrow;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

import com.naminhyeok.fantazzk.CoreException;
import com.naminhyeok.fantazzk.ErrorDescriptor;
import com.naminhyeok.fantazzk.ErrorMessage;
import com.naminhyeok.fantazzk.GlobalExceptionHandler;
import com.naminhyeok.fantazzk.room.RoomApiTestFixtures;
import com.naminhyeok.fantazzk.room.RoomStartApi;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.assertj.MockMvcTester;
import org.slf4j.event.Level;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

@WebMvcTest(RoomStartApiController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import(GlobalExceptionHandler.class)
class RoomStartApiControllerWebMvcTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private RoomStartApi roomStartApi;

    @Test
    void start는_header가_있으면_성공한다() throws Exception {
        given(roomStartApi.start(RoomApiTestFixtures.ROOM_CODE, RoomApiTestFixtures.HOST_TOKEN))
            .willReturn(RoomApiTestFixtures.startedAuctionGameView());

        var result = mockMvcTester().perform(
            post("/api/v1/rooms/{code}/start", RoomApiTestFixtures.ROOM_CODE)
                .header("X-Room-Action-Token", RoomApiTestFixtures.HOST_TOKEN)
        );

        result.assertThat().hasStatusOk();
        JsonNode body = objectMapper.readTree(result.getResponse().getContentAsString());
        assertThat(body.at("/resultType").asText()).isEqualTo("SUCCESS");
        assertThat(body.at("/success/id").asText()).isEqualTo(RoomApiTestFixtures.GAME_ID);
        assertThat(body.at("/success/roomCode").asText()).isEqualTo(RoomApiTestFixtures.ROOM_CODE);
        assertThat(body.at("/success/status").asText()).isEqualTo("IN_PROGRESS");
        assertThat(body.at("/success/startReadiness").isMissingNode()).isTrue();
    }

    @Test
    void start는_header가_없으면_401을_반환한다() throws Exception {
        doThrow(coreException(HttpStatus.UNAUTHORIZED, "ROOM_ACTION_TOKEN_REQUIRED"))
            .when(roomStartApi)
            .start(RoomApiTestFixtures.ROOM_CODE, null);

        var result = mockMvcTester().perform(post("/api/v1/rooms/{code}/start", RoomApiTestFixtures.ROOM_CODE));

        result.assertThat().hasStatus(HttpStatus.UNAUTHORIZED);
        assertThat(readBody(result, VoidApiResponse.class).error().code()).isEqualTo("ROOM_ACTION_TOKEN_REQUIRED");
    }

    @Test
    void start는_optimistic_lock_conflict를_409로_반환한다() throws Exception {
        doThrow(coreException(HttpStatus.CONFLICT, "ROOM_CONCURRENT_MODIFICATION"))
            .when(roomStartApi)
            .start(RoomApiTestFixtures.ROOM_CODE, RoomApiTestFixtures.HOST_TOKEN);

        var result = mockMvcTester().perform(
            post("/api/v1/rooms/{code}/start", RoomApiTestFixtures.ROOM_CODE)
                .header("X-Room-Action-Token", RoomApiTestFixtures.HOST_TOKEN)
        );

        result.assertThat().hasStatus(HttpStatus.CONFLICT);
        assertThat(readBody(result, VoidApiResponse.class).error().code()).isEqualTo("ROOM_CONCURRENT_MODIFICATION");
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
