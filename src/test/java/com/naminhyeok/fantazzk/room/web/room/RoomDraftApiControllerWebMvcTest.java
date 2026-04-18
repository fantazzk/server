package com.naminhyeok.fantazzk.room.web.room;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doThrow;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;

import com.naminhyeok.fantazzk.CoreException;
import com.naminhyeok.fantazzk.ErrorDescriptor;
import com.naminhyeok.fantazzk.ErrorMessage;
import com.naminhyeok.fantazzk.GlobalExceptionHandler;
import com.naminhyeok.fantazzk.room.RoomApiTestFixtures;
import com.naminhyeok.fantazzk.room.RoomDraftApi;
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

@WebMvcTest(RoomDraftApiController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import(GlobalExceptionHandler.class)
class RoomDraftApiControllerWebMvcTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private RoomDraftApi roomDraftApi;

    @Test
    void selectDraftPosition은_header가_있으면_성공한다() throws Exception {
        given(roomDraftApi.selectDraftPosition(RoomApiTestFixtures.ROOM_CODE, RoomApiTestFixtures.GUEST_TOKEN, 2))
            .willReturn(RoomApiTestFixtures.selectedDraftPositionRoomView());

        var result = mockMvcTester().perform(
            put("/api/v1/rooms/{code}/draft-position", RoomApiTestFixtures.ROOM_CODE)
                .header("X-Room-Action-Token", RoomApiTestFixtures.GUEST_TOKEN)
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    """
                    {
                      "draftPosition": 2
                    }
                    """
                )
        );

        result.assertThat().hasStatusOk();
        JsonNode body = objectMapper.readTree(result.getResponse().getContentAsString());
        assertThat(body.at("/success/startReadiness").asText()).isEqualTo("STARTABLE");
        assertThat(body.at("/success/code").asText()).isEqualTo(RoomApiTestFixtures.ROOM_CODE);
        assertThat(body.at("/success/progress").isMissingNode()).isTrue();
        assertThat(body.at("/success/members").isMissingNode()).isTrue();
    }

    @Test
    void clearDraftPosition은_header가_있으면_성공한다() throws Exception {
        given(roomDraftApi.clearDraftPosition(RoomApiTestFixtures.ROOM_CODE, RoomApiTestFixtures.HOST_TOKEN))
            .willReturn(RoomApiTestFixtures.clearedDraftPositionRoomView());

        var result = mockMvcTester().perform(
            delete("/api/v1/rooms/{code}/draft-position", RoomApiTestFixtures.ROOM_CODE)
                .header("X-Room-Action-Token", RoomApiTestFixtures.HOST_TOKEN)
        );

        result.assertThat().hasStatusOk();
        JsonNode body = objectMapper.readTree(result.getResponse().getContentAsString());
        assertThat(body.at("/success/startReadiness").asText()).isEqualTo("WAITING_FOR_DRAFT_POSITIONS");
        assertThat(body.at("/success/code").asText()).isEqualTo(RoomApiTestFixtures.ROOM_CODE);
        assertThat(body.at("/success/progress").isMissingNode()).isTrue();
        assertThat(body.at("/success/members").isMissingNode()).isTrue();
    }

    @Test
    void selectDraftPosition은_header가_없으면_401을_반환한다() throws Exception {
        doThrow(coreException(HttpStatus.UNAUTHORIZED, "ROOM_ACTION_TOKEN_REQUIRED"))
            .when(roomDraftApi)
            .selectDraftPosition(RoomApiTestFixtures.ROOM_CODE, null, 2);

        var result = mockMvcTester().perform(
            put("/api/v1/rooms/{code}/draft-position", RoomApiTestFixtures.ROOM_CODE)
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    """
                    {
                      "draftPosition": 2
                    }
                    """
                )
        );

        result.assertThat().hasStatus(HttpStatus.UNAUTHORIZED);
        assertThat(readBody(result, VoidApiResponse.class).error().code()).isEqualTo("ROOM_ACTION_TOKEN_REQUIRED");
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
