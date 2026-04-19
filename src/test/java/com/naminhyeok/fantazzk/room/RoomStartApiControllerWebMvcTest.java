package com.naminhyeok.fantazzk.room;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doThrow;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

import com.naminhyeok.fantazzk.CoreException;
import com.naminhyeok.fantazzk.ErrorMessage;
import com.naminhyeok.fantazzk.GlobalExceptionHandler;
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

@WebMvcTest(RoomStartApiController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import(GlobalExceptionHandler.class)
class RoomStartApiControllerWebMvcTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private StartRoom startRoom;

    @Test
    void start는_header가_있으면_성공한다() throws Exception {
        given(startRoom.start(RoomApiTestFixtures.ROOM_CODE, RoomApiTestFixtures.HOST_TOKEN))
            .willReturn(RoomApiTestFixtures.startedAuctionDetails().game());

        var result = mockMvcTester().perform(
            post("/api/v1/rooms/{code}/start", RoomApiTestFixtures.ROOM_CODE)
                .header("X-Room-Action-Token", RoomApiTestFixtures.HOST_TOKEN)
        );

        result.assertThat().hasStatusOk();
        JsonNode body = objectMapper.readTree(result.getResponse().getContentAsString());
        assertThat(body.at("/resultType").asText()).isEqualTo("SUCCESS");
        assertThat(body.at("/success/gameId").asText()).isEqualTo(RoomApiTestFixtures.GAME_ID);
        assertThat(body.at("/success/roomCode").isMissingNode()).isTrue();
        assertThat(body.at("/success/mode").isMissingNode()).isTrue();
        assertThat(body.at("/success/status").isMissingNode()).isTrue();
    }

    @Test
    void start는_header가_없으면_401을_반환한다() throws Exception {
        doThrow(CoreException.of(RoomErrorType.ROOM_ACTION_TOKEN_REQUIRED))
            .when(startRoom)
            .start(RoomApiTestFixtures.ROOM_CODE, null);

        var result = mockMvcTester().perform(post("/api/v1/rooms/{code}/start", RoomApiTestFixtures.ROOM_CODE));

        result.assertThat().hasStatus(HttpStatus.UNAUTHORIZED);
        assertThat(readBody(result, VoidApiResponse.class).error().code()).isEqualTo("ROOM_ACTION_TOKEN_REQUIRED");
    }

    @Test
    void start는_optimistic_lock_conflict를_409로_반환한다() throws Exception {
        doThrow(CoreException.of(RoomErrorType.ROOM_CONCURRENT_MODIFICATION))
            .when(startRoom)
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

    private record VoidApiResponse(String resultType, Void success, ErrorMessage error) {}
}
