package com.naminhyeok.fantazzk.room.web;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doThrow;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

import com.naminhyeok.fantazzk.CoreException;
import com.naminhyeok.fantazzk.ErrorMessage;
import com.naminhyeok.fantazzk.GlobalExceptionHandler;
import com.naminhyeok.fantazzk.room.application.PlaceBid;
import com.naminhyeok.fantazzk.room.domain.RoomErrorType;
import com.naminhyeok.fantazzk.room.domain.RoomStateInvalidException;
import com.naminhyeok.fantazzk.room.support.RoomApiTestFixtures;
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
import tools.jackson.databind.ObjectMapper;

@WebMvcTest(GameAuctionApiController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import(GlobalExceptionHandler.class)
class GameAuctionApiControllerWebMvcTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private PlaceBid placeBid;

    @Test
    void 입찰_API는_액션_토큰이_있으면_성공한다() throws Exception {
        var result = mockMvcTester().perform(
            post("/api/v1/games/{gameId}/bids", RoomApiTestFixtures.GAME_ID)
                .header("X-Room-Action-Token", RoomApiTestFixtures.HOST_TOKEN)
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    """
                    {
                      "amount": 150
                    }
                    """
                )
        );

        result.assertThat().hasStatusOk();
    }

    @Test
    void 입찰_API는_액션_토큰이_없으면_401을_반환한다() throws Exception {
        UUID gameId = UUID.fromString(RoomApiTestFixtures.GAME_ID);
        doThrow(CoreException.of(RoomErrorType.ROOM_ACTION_TOKEN_REQUIRED))
            .when(placeBid)
            .place(gameId, null, 150);

        var result = mockMvcTester().perform(
            post("/api/v1/games/{gameId}/bids", RoomApiTestFixtures.GAME_ID)
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    """
                    {
                      "amount": 150
                    }
                    """
                )
        );

        result.assertThat().hasStatus(HttpStatus.UNAUTHORIZED);
        assertThat(readBody(result, VoidApiResponse.class).error().code()).isEqualTo("ROOM_ACTION_TOKEN_REQUIRED");
    }

    @Test
    void 입찰_API는_0원_입찰을_400으로_거부한다() throws Exception {
        var result = mockMvcTester().perform(
            post("/api/v1/games/{gameId}/bids", RoomApiTestFixtures.GAME_ID)
                .header("X-Room-Action-Token", RoomApiTestFixtures.HOST_TOKEN)
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    """
                    {
                      "amount": 0
                    }
                    """
                )
        );

        result.assertThat().hasStatus(HttpStatus.BAD_REQUEST);
        assertThat(readBody(result, VoidApiResponse.class).error().code()).isEqualTo("BAD_REQUEST");
    }

    @Test
    void 입찰_API는_동시_수정_충돌을_409로_반환한다() throws Exception {
        UUID gameId = UUID.fromString(RoomApiTestFixtures.GAME_ID);
        doThrow(CoreException.of(RoomErrorType.ROOM_CONCURRENT_MODIFICATION))
            .when(placeBid)
            .place(gameId, RoomApiTestFixtures.HOST_TOKEN, 150);

        var result = mockMvcTester().perform(
            post("/api/v1/games/{gameId}/bids", RoomApiTestFixtures.GAME_ID)
                .header("X-Room-Action-Token", RoomApiTestFixtures.HOST_TOKEN)
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    """
                    {
                      "amount": 150
                    }
                    """
                )
        );

        result.assertThat().hasStatus(HttpStatus.CONFLICT);
        assertThat(readBody(result, VoidApiResponse.class).error().code()).isEqualTo("ROOM_CONCURRENT_MODIFICATION");
    }

    @Test
    void 입찰_API는_손상된_게임_상태를_500으로_반환한다() throws Exception {
        UUID gameId = UUID.fromString(RoomApiTestFixtures.GAME_ID);
        doThrow(RoomStateInvalidException.auctionRoundMissing())
            .when(placeBid)
            .place(gameId, RoomApiTestFixtures.HOST_TOKEN, 150);

        var result = mockMvcTester().perform(
            post("/api/v1/games/{gameId}/bids", RoomApiTestFixtures.GAME_ID)
                .header("X-Room-Action-Token", RoomApiTestFixtures.HOST_TOKEN)
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    """
                    {
                      "amount": 150
                    }
                    """
                )
        );

        result.assertThat().hasStatus(HttpStatus.INTERNAL_SERVER_ERROR);
        VoidApiResponse body = readBody(result, VoidApiResponse.class);
        assertThat(body.error().code()).isEqualTo("ROOM_STATE_INVALID");
        assertThat(body.error().message()).isEqualTo("방 상태가 올바르지 않습니다. 잠시 후 다시 시도해 주세요");
        assertThat(body.error().data()).isNull();
    }

    @Test
    void 입찰_API는_최소_입찰_증가폭을_만족하지_못하면_409를_반환한다() throws Exception {
        UUID gameId = UUID.fromString(RoomApiTestFixtures.GAME_ID);
        doThrow(CoreException.of(RoomErrorType.ROOM_BID_MIN_UNIT_NOT_MET))
            .when(placeBid)
            .place(gameId, RoomApiTestFixtures.HOST_TOKEN, 105);

        var result = mockMvcTester().perform(
            post("/api/v1/games/{gameId}/bids", RoomApiTestFixtures.GAME_ID)
                .header("X-Room-Action-Token", RoomApiTestFixtures.HOST_TOKEN)
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    """
                    {
                      "amount": 105
                    }
                    """
                )
        );

        result.assertThat().hasStatus(HttpStatus.CONFLICT);
        assertThat(readBody(result, VoidApiResponse.class).error().code()).isEqualTo("ROOM_BID_MIN_UNIT_NOT_MET");
    }

    private MockMvcTester mockMvcTester() {
        return MockMvcTester.create(mockMvc);
    }

    private <T> T readBody(org.springframework.test.web.servlet.assertj.MvcTestResult result, Class<T> bodyType) throws Exception {
        return objectMapper.readValue(result.getResponse().getContentAsString(), bodyType);
    }

    private record VoidApiResponse(String resultType, Void success, ErrorMessage error) {}
}
