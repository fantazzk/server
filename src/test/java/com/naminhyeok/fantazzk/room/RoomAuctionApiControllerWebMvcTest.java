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
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.assertj.MockMvcTester;
import tools.jackson.databind.ObjectMapper;

@WebMvcTest(RoomAuctionApiController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import(GlobalExceptionHandler.class)
class RoomAuctionApiControllerWebMvcTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private StartRoom startRoom;

    @MockitoBean
    private PlaceBid placeBid;

    @MockitoBean
    private SettleAuction settleAuction;

    @MockitoBean
    private GetRoomDetails getRoomDetails;

    @Test
    void start는_header가_있으면_성공한다() throws Exception {
        given(startRoom.start(RoomApiTestFixtures.ROOM_CODE, RoomApiTestFixtures.HOST_TOKEN))
            .willReturn(RoomApiTestFixtures.startedAuctionDetails());

        var result = mockMvcTester().perform(
            post("/api/v1/rooms/{code}/start", RoomApiTestFixtures.ROOM_CODE)
                .header("X-Room-Action-Token", RoomApiTestFixtures.HOST_TOKEN)
        );

        result.assertThat().hasStatusOk();
        RoomResponseApiResponse body = readBody(result, RoomResponseApiResponse.class);
        assertThat(body.resultType()).isEqualTo("SUCCESS");
        assertThat(body.success().status()).isEqualTo("IN_PROGRESS");
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

    @Test
    void placeBid는_header가_있으면_최신_room_snapshot을_반환한다() throws Exception {
        given(placeBid.place(RoomApiTestFixtures.ROOM_CODE, RoomApiTestFixtures.HOST_TOKEN, 150))
            .willReturn(new RoomBid(1, new BidSequence(1), new TeamLeaderId(RoomApiTestFixtures.HOST_ID), 150));
        given(getRoomDetails.get(RoomApiTestFixtures.ROOM_CODE))
            .willReturn(RoomApiTestFixtures.startedAuctionDetails());

        var result = mockMvcTester().perform(
            post("/api/v1/rooms/{code}/bids", RoomApiTestFixtures.ROOM_CODE)
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
        RoomResponseApiResponse body = readBody(result, RoomResponseApiResponse.class);
        assertThat(body.resultType()).isEqualTo("SUCCESS");
        assertThat(body.success().status()).isEqualTo("IN_PROGRESS");
        assertThat(body.success().code()).isEqualTo(RoomApiTestFixtures.ROOM_CODE);
    }

    @Test
    void placeBid는_header가_없으면_401을_반환한다() throws Exception {
        doThrow(CoreException.of(RoomErrorType.ROOM_ACTION_TOKEN_REQUIRED))
            .when(placeBid)
            .place(RoomApiTestFixtures.ROOM_CODE, null, 150);

        var result = mockMvcTester().perform(
            post("/api/v1/rooms/{code}/bids", RoomApiTestFixtures.ROOM_CODE)
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
            post("/api/v1/rooms/{code}/bids", RoomApiTestFixtures.ROOM_CODE)
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
    void placeBid는_amount가_없으면_400을_반환한다() throws Exception {
        var result = mockMvcTester().perform(
            post("/api/v1/rooms/{code}/bids", RoomApiTestFixtures.ROOM_CODE)
                .header("X-Room-Action-Token", RoomApiTestFixtures.HOST_TOKEN)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}")
        );

        result.assertThat().hasStatus(HttpStatus.BAD_REQUEST);
        assertThat(readBody(result, VoidApiResponse.class).error().code()).isEqualTo("BAD_REQUEST");
    }

    @Test
    void placeBid는_optimistic_lock_conflict를_409로_반환한다() throws Exception {
        doThrow(CoreException.of(RoomErrorType.ROOM_CONCURRENT_MODIFICATION))
            .when(placeBid)
            .place(RoomApiTestFixtures.ROOM_CODE, RoomApiTestFixtures.HOST_TOKEN, 150);

        var result = mockMvcTester().perform(
            post("/api/v1/rooms/{code}/bids", RoomApiTestFixtures.ROOM_CODE)
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
        doThrow(RoomStateInvalidException.auctionRoundMissing())
            .when(placeBid)
            .place(RoomApiTestFixtures.ROOM_CODE, RoomApiTestFixtures.HOST_TOKEN, 150);

        var result = mockMvcTester().perform(
            post("/api/v1/rooms/{code}/bids", RoomApiTestFixtures.ROOM_CODE)
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
        doThrow(CoreException.of(RoomErrorType.ROOM_BID_MIN_UNIT_NOT_MET))
            .when(placeBid)
            .place(RoomApiTestFixtures.ROOM_CODE, RoomApiTestFixtures.HOST_TOKEN, 105);

        var result = mockMvcTester().perform(
            post("/api/v1/rooms/{code}/bids", RoomApiTestFixtures.ROOM_CODE)
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
    void auctionProgress는_settle된_room의_latest_snapshot을_반환한다() throws Exception {
        given(settleAuction.settleIfDue(RoomApiTestFixtures.ROOM_CODE)).willReturn(RoomApiTestFixtures.inProgressAuctionRoom());
        given(getRoomDetails.get(RoomApiTestFixtures.ROOM_CODE)).willReturn(RoomApiTestFixtures.inProgressAuctionDetails());

        var result = mockMvcTester().perform(
            post("/api/v1/rooms/{code}/auction/progress", RoomApiTestFixtures.ROOM_CODE)
        );

        result.assertThat().hasStatusOk();
        RoomResponseApiResponse body = readBody(result, RoomResponseApiResponse.class);
        assertThat(body.resultType()).isEqualTo("SUCCESS");
        assertThat(body.success().status()).isEqualTo("IN_PROGRESS");
        assertThat(body.success().code()).isEqualTo(RoomApiTestFixtures.ROOM_CODE);
    }

    private MockMvcTester mockMvcTester() {
        return MockMvcTester.create(mockMvc);
    }

    private <T> T readBody(org.springframework.test.web.servlet.assertj.MvcTestResult result, Class<T> bodyType) throws Exception {
        return objectMapper.readValue(result.getResponse().getContentAsString(), bodyType);
    }

    private record RoomResponseApiResponse(String resultType, RoomResponse success, ErrorMessage error) {}

    private record VoidApiResponse(String resultType, Void success, ErrorMessage error) {}
}
