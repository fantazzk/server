package com.naminhyeok.fantazzk;

import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.slf4j.event.Level;

class ApiResponseTest {
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void 성공_응답은_SUCCESS와_payload를_공통_envelope에_담는다() throws Exception {
        ApiResponse<String> response = ApiResponse.success("ok");
        JsonNode json = objectMapper.readTree(objectMapper.writeValueAsString(response));

        assertThat(response.getResultType()).isEqualTo(ResultType.SUCCESS);
        assertThat(response.getSuccess()).isEqualTo("ok");
        assertThat(response.getError()).isNull();
        assertThat(json.get("resultType").asText()).isEqualTo("SUCCESS");
        assertThat(json.get("success").asText()).isEqualTo("ok");
        assertThat(json.get("error").isNull()).isTrue();
    }

    @Test
    void 실패_응답은_descriptor_기반_error_envelope을_사용한다() throws Exception {
        ApiResponse<Void> response = ApiResponse.error(new FakeErrorDescriptor(), "detail");
        JsonNode json = objectMapper.readTree(objectMapper.writeValueAsString(response));

        assertThat(response.getResultType()).isEqualTo(ResultType.ERROR);
        assertThat(response.getSuccess()).isNull();
        assertThat(response.getError().code()).isEqualTo("BAD_REQUEST");
        assertThat(response.getError().message()).isEqualTo("잘못된 요청입니다");
        assertThat(response.getError().data()).isEqualTo("detail");
        assertThat(json.get("resultType").asText()).isEqualTo("ERROR");
        assertThat(json.get("success").isNull()).isTrue();
        assertThat(json.get("error").get("code").asText()).isEqualTo("BAD_REQUEST");
        assertThat(json.get("error").get("message").asText()).isEqualTo("잘못된 요청입니다");
        assertThat(json.get("error").get("data").asText()).isEqualTo("detail");
    }

    private static final class FakeErrorDescriptor implements ErrorDescriptor {

        @Override
        public HttpStatus getStatus() {
            return HttpStatus.BAD_REQUEST;
        }

        @Override
        public String getCode() {
            return "BAD_REQUEST";
        }

        @Override
        public String getMessage() {
            return "잘못된 요청입니다";
        }

        @Override
        public Level getLogLevel() {
            return Level.WARN;
        }
    }
}
