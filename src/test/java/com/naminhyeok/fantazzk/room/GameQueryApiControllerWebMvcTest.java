package com.naminhyeok.fantazzk.room;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

import com.naminhyeok.fantazzk.CoreException;
import com.naminhyeok.fantazzk.ErrorMessage;
import com.naminhyeok.fantazzk.GlobalExceptionHandler;
import java.util.UUID;
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

@WebMvcTest(GameQueryApiController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import(GlobalExceptionHandler.class)
class GameQueryApiControllerWebMvcTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private GetGame getGame;

    @Test
    void get은_started_game_snapshot을_반환한다() throws Exception {
        given(getGame.get(UUID.fromString(RoomApiTestFixtures.GAME_ID)))
            .willReturn(RoomApiTestFixtures.inProgressAuctionDetails().game());

        var result = mockMvcTester().perform(get("/api/v1/games/{gameId}", RoomApiTestFixtures.GAME_ID));

        result.assertThat().hasStatusOk();
        JsonNode body = objectMapper.readTree(result.getResponse().getContentAsString());
        assertThat(body.at("/resultType").asText()).isEqualTo("SUCCESS");
        assertThat(body.at("/success/id").asText()).isEqualTo(RoomApiTestFixtures.GAME_ID);
        assertThat(body.at("/success/status").asText()).isEqualTo("IN_PROGRESS");
        assertThat(body.at("/success/progress/currentRound").asInt()).isEqualTo(2);
    }

    @Test
    void get은_game이_없으면_404를_반환한다() throws Exception {
        given(getGame.get(UUID.fromString(RoomApiTestFixtures.GAME_ID))).willThrow(CoreException.of(RoomErrorType.GAME_NOT_FOUND));

        var result = mockMvcTester().perform(get("/api/v1/games/{gameId}", RoomApiTestFixtures.GAME_ID));

        result.assertThat().hasStatus(HttpStatus.NOT_FOUND);
        assertThat(readBody(result, VoidApiResponse.class).error().code()).isEqualTo("GAME_NOT_FOUND");
    }

    private MockMvcTester mockMvcTester() {
        return MockMvcTester.create(mockMvc);
    }

    private <T> T readBody(org.springframework.test.web.servlet.assertj.MvcTestResult result, Class<T> bodyType) throws Exception {
        return objectMapper.readValue(result.getResponse().getContentAsString(), bodyType);
    }

    private record VoidApiResponse(String resultType, Void success, ErrorMessage error) {}
}
