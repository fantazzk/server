package com.naminhyeok.fantazzk.room;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doThrow;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

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
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.assertj.MockMvcTester;
import tools.jackson.databind.JsonNode;
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

    @MockitoBean
    private SettleAuction settleAuction;

    @MockitoBean
    private GetGame getGame;

    @Test
    void placeBid는_header가_있으면_최신_game_snapshot을_반환한다() throws Exception {
        UUID gameId = UUID.fromString(RoomApiTestFixtures.GAME_ID);
        given(placeBid.place(gameId, RoomApiTestFixtures.HOST_TOKEN, 150))
            .willReturn(new AuctionBid(1, new BidSequence(1), new TeamLeaderId(RoomApiTestFixtures.HOST_ID), 150));
        given(getGame.get(gameId)).willReturn(RoomApiTestFixtures.inProgressAuctionDetails().game());

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
        JsonNode body = objectMapper.readTree(result.getResponse().getContentAsString());
        assertThat(body.at("/resultType").asText()).isEqualTo("SUCCESS");
        assertThat(body.at("/success/id").asText()).isEqualTo(RoomApiTestFixtures.GAME_ID);
        assertThat(body.at("/success/status").asText()).isEqualTo("IN_PROGRESS");
    }

    @Test
    void placeBid는_header가_없으면_401을_반환한다() throws Exception {
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
    void placeBid는_0원이면_400을_반환한다() throws Exception {
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
    void placeBid는_optimistic_lock_conflict를_409로_반환한다() throws Exception {
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
    void placeBid는_내부_상태_예외를_500으로_반환한다() throws Exception {
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
    void placeBid는_최소_입찰_증가폭을_만족하지_못하면_409를_반환한다() throws Exception {
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

    @Test
    void auctionProgress는_settle된_game의_latest_snapshot을_반환한다() throws Exception {
        UUID gameId = UUID.fromString(RoomApiTestFixtures.GAME_ID);
        given(settleAuction.settleIfDue(gameId)).willReturn(RoomApiTestFixtures.inProgressAuctionDetails().game());

        var result = mockMvcTester().perform(post("/api/v1/games/{gameId}/auction/progress", RoomApiTestFixtures.GAME_ID));

        result.assertThat().hasStatusOk();
        JsonNode body = objectMapper.readTree(result.getResponse().getContentAsString());
        assertThat(body.at("/resultType").asText()).isEqualTo("SUCCESS");
        assertThat(body.at("/success/id").asText()).isEqualTo(RoomApiTestFixtures.GAME_ID);
        assertThat(body.at("/success/status").asText()).isEqualTo("IN_PROGRESS");
    }

    private MockMvcTester mockMvcTester() {
        return MockMvcTester.create(mockMvc);
    }

    private <T> T readBody(org.springframework.test.web.servlet.assertj.MvcTestResult result, Class<T> bodyType) throws Exception {
        return objectMapper.readValue(result.getResponse().getContentAsString(), bodyType);
    }

    private record VoidApiResponse(String resultType, Void success, ErrorMessage error) {}
}
