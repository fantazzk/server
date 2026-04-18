package com.naminhyeok.fantazzk.room.web.game;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doThrow;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

import com.naminhyeok.fantazzk.CoreException;
import com.naminhyeok.fantazzk.ErrorDescriptor;
import com.naminhyeok.fantazzk.ErrorMessage;
import com.naminhyeok.fantazzk.GlobalExceptionHandler;
import com.naminhyeok.fantazzk.room.GameDraftApi;
import com.naminhyeok.fantazzk.room.RoomApiTestFixtures;
import java.util.UUID;
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
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

@WebMvcTest(GameDraftApiController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import(GlobalExceptionHandler.class)
class GameDraftApiControllerWebMvcTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private GameDraftApi gameDraftApi;

    @Test
    void pickDraft는_header가_있으면_최신_game_snapshot을_반환한다() throws Exception {
        UUID gameId = UUID.fromString(RoomApiTestFixtures.DRAFT_GAME_ID);
        given(gameDraftApi.pick(gameId, RoomApiTestFixtures.GUEST_TOKEN, "선수3"))
            .willReturn(RoomApiTestFixtures.inProgressDraftGameView());

        var result = mockMvcTester().perform(
            post("/api/v1/games/{gameId}/draft-picks", RoomApiTestFixtures.DRAFT_GAME_ID)
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
        JsonNode body = objectMapper.readTree(result.getResponse().getContentAsString());
        assertThat(body.at("/resultType").asText()).isEqualTo("SUCCESS");
        assertThat(body.at("/success/id").asText()).isEqualTo(RoomApiTestFixtures.DRAFT_GAME_ID);
        assertThat(body.at("/success/status").asText()).isEqualTo("IN_PROGRESS");
    }

    @Test
    void pickDraft는_header가_없으면_401을_반환한다() throws Exception {
        UUID gameId = UUID.fromString(RoomApiTestFixtures.DRAFT_GAME_ID);
        doThrow(coreException(HttpStatus.UNAUTHORIZED, "ROOM_ACTION_TOKEN_REQUIRED"))
            .when(gameDraftApi)
            .pick(gameId, null, "선수3");

        var result = mockMvcTester().perform(
            post("/api/v1/games/{gameId}/draft-picks", RoomApiTestFixtures.DRAFT_GAME_ID)
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
        UUID gameId = UUID.fromString(RoomApiTestFixtures.DRAFT_GAME_ID);
        doThrow(coreException(HttpStatus.CONFLICT, "ROOM_CONCURRENT_MODIFICATION"))
            .when(gameDraftApi)
            .pick(gameId, RoomApiTestFixtures.GUEST_TOKEN, "선수3");

        var result = mockMvcTester().perform(
            post("/api/v1/games/{gameId}/draft-picks", RoomApiTestFixtures.DRAFT_GAME_ID)
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
