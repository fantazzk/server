package com.naminhyeok.fantazzk;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

class GlobalExceptionHandlerTest {
    private final ObjectMapper objectMapper = new ObjectMapper();

    private MockMvc mockMvc;
    private Logger handlerLogger;
    private ListAppender<ILoggingEvent> logAppender;

    @BeforeEach
    void setUp() {
        handlerLogger = (Logger) LoggerFactory.getLogger(GlobalExceptionHandler.class);
        logAppender = new ListAppender<>();
        logAppender.start();
        handlerLogger.addAppender(logAppender);
        mockMvc = MockMvcBuilders.standaloneSetup(new ThrowingController())
            .setControllerAdvice(new GlobalExceptionHandler())
            .build();
    }

    @AfterEach
    void tearDown() {
        handlerLogger.detachAppender(logAppender);
        logAppender.stop();
    }

    @Test
    void core_exception_renders_error_response_from_descriptor_and_data() throws Exception {
        String response = mockMvc.perform(get("/core"))
            .andExpect(status().isConflict())
            .andExpect(jsonPath("$.resultType").value("ERROR"))
            .andExpect(jsonPath("$.error.code").value("CONFLICT_ERROR"))
            .andExpect(jsonPath("$.error.message").value("충돌이 발생했습니다"))
            .andExpect(jsonPath("$.error.data.detail").value("conflict"))
            .andReturn()
            .getResponse()
            .getContentAsString();

        JsonNode json = objectMapper.readTree(response);

        assertThat(json.get("error").get("code").asText()).isEqualTo("CONFLICT_ERROR");
        assertThat(json.get("error").get("message").asText()).isEqualTo("충돌이 발생했습니다");
        assertThat(json.get("error").get("data").get("detail").asText()).isEqualTo("conflict");
        assertLastLog(Level.WARN);
    }

    @Test
    void room_exception_renders_room_error_response_from_descriptor_and_data() throws Exception {
        String response = mockMvc.perform(get("/room"))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.resultType").value("ERROR"))
            .andExpect(jsonPath("$.error.code").value("ROOM_NOT_FOUND"))
            .andExpect(jsonPath("$.error.message").value("방을 찾을 수 없습니다"))
            .andReturn()
            .getResponse()
            .getContentAsString();

        JsonNode json = objectMapper.readTree(response);

        assertThat(json.get("error").get("code").asText()).isEqualTo("ROOM_NOT_FOUND");
        assertThat(json.get("error").get("message").asText()).isEqualTo("방을 찾을 수 없습니다");
        assertThat(json.get("error").get("data").isNull()).isTrue();
        assertLastLog(Level.WARN);
    }

    @Test
    void illegal_argument_exception_renders_bad_request_error_response() throws Exception {
        String response = mockMvc.perform(get("/illegal"))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.resultType").value("ERROR"))
            .andExpect(jsonPath("$.error.code").value("BAD_REQUEST"))
            .andExpect(jsonPath("$.error.message").value("요청이 올바르지 않습니다"))
            .andReturn()
            .getResponse()
            .getContentAsString();

        JsonNode json = objectMapper.readTree(response);

        assertThat(json.get("error").get("code").asText()).isEqualTo("BAD_REQUEST");
        assertThat(json.get("error").get("message").asText()).isEqualTo("요청이 올바르지 않습니다");
        assertThat(json.get("error").get("data").isNull()).isTrue();
        assertLastLog(Level.WARN);
    }

    @Test
    void illegal_argument_exception_with_null_message_renders_bad_request_without_detail_map() throws Exception {
        String response = mockMvc.perform(get("/illegal-null-message"))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.resultType").value("ERROR"))
            .andExpect(jsonPath("$.error.code").value("BAD_REQUEST"))
            .andExpect(jsonPath("$.error.message").value("요청이 올바르지 않습니다"))
            .andReturn()
            .getResponse()
            .getContentAsString();

        JsonNode json = objectMapper.readTree(response);

        assertThat(json.get("error").get("data").isNull()).isTrue();
        assertLastLog(Level.WARN);
    }

    @Test
    void generic_exception_renders_internal_server_error_response() throws Exception {
        String response = mockMvc.perform(get("/generic"))
            .andExpect(status().isInternalServerError())
            .andExpect(jsonPath("$.resultType").value("ERROR"))
            .andExpect(jsonPath("$.error.code").value("INTERNAL_SERVER_ERROR"))
            .andExpect(jsonPath("$.error.message").value("예기치 못한 오류가 발생했습니다"))
            .andReturn()
            .getResponse()
            .getContentAsString();

        JsonNode json = objectMapper.readTree(response);

        assertThat(json.get("error").get("code").asText()).isEqualTo("INTERNAL_SERVER_ERROR");
        assertThat(json.get("error").get("message").asText()).isEqualTo("예기치 못한 오류가 발생했습니다");
        assertThat(json.get("error").get("data").isNull()).isTrue();
        assertLastLog(Level.ERROR);
    }

    @Test
    void validation_exception_renders_field_error_map_in_data() throws Exception {
        String response = mockMvc.perform(
                post("/validation")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("""
                        {
                          "name": "",
                          "age": 0
                        }
                        """)
            )
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.resultType").value("ERROR"))
            .andExpect(jsonPath("$.error.code").value("BAD_REQUEST"))
            .andExpect(jsonPath("$.error.message").value("요청이 올바르지 않습니다"))
            .andExpect(jsonPath("$.error.data.name").value("이름은 필수입니다"))
            .andExpect(jsonPath("$.error.data.age").value("나이는 1 이상이어야 합니다"))
            .andReturn()
            .getResponse()
            .getContentAsString();

        JsonNode json = objectMapper.readTree(response);

        assertThat(json.get("error").get("data").get("name").asText()).isEqualTo("이름은 필수입니다");
        assertThat(json.get("error").get("data").get("age").asText()).isEqualTo("나이는 1 이상이어야 합니다");
        assertLastLog(Level.WARN);
    }

    @RestController
    static class ThrowingController {
        @GetMapping("/core")
        String core() {
            throw new CoreException(new ConflictErrorDescriptor(), java.util.Map.of("detail", "conflict"));
        }

        @GetMapping("/room")
        String room() {
            throw CoreException.of(new MissingRoomErrorDescriptor());
        }

        @GetMapping("/illegal")
        String illegal() {
            throw new IllegalArgumentException("bad argument");
        }

        @GetMapping("/illegal-null-message")
        String illegalNullMessage() {
            throw new IllegalArgumentException();
        }

        @GetMapping("/generic")
        String generic() {
            throw new RuntimeException("boom");
        }

        @PostMapping("/validation")
        String validation(@Valid @RequestBody ValidationRequest request) {
            return "ok";
        }
    }

    record ValidationRequest(
        @NotBlank(message = "이름은 필수입니다") String name,
        @Min(value = 1, message = "나이는 1 이상이어야 합니다") int age
    ) {
    }

    private static final class ConflictErrorDescriptor implements ErrorDescriptor {
        @Override
        public HttpStatus getStatus() {
            return HttpStatus.CONFLICT;
        }

        @Override
        public String getCode() {
            return "CONFLICT_ERROR";
        }

        @Override
        public String getMessage() {
            return "충돌이 발생했습니다";
        }

        @Override
        public org.slf4j.event.Level getLogLevel() {
            return org.slf4j.event.Level.WARN;
        }
    }

    private static final class MissingRoomErrorDescriptor implements ErrorDescriptor {
        @Override
        public HttpStatus getStatus() {
            return HttpStatus.NOT_FOUND;
        }

        @Override
        public String getCode() {
            return "ROOM_NOT_FOUND";
        }

        @Override
        public String getMessage() {
            return "방을 찾을 수 없습니다";
        }

        @Override
        public org.slf4j.event.Level getLogLevel() {
            return org.slf4j.event.Level.WARN;
        }
    }

    private void assertLastLog(Level expectedLevel) {
        assertThat(logAppender.list).isNotEmpty();
        assertThat(logAppender.list.get(logAppender.list.size() - 1).getLevel()).isEqualTo(expectedLevel);
    }
}
