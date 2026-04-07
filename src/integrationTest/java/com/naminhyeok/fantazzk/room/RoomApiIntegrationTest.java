package com.naminhyeok.fantazzk.room;

import static org.assertj.core.api.Assertions.assertThat;

import com.naminhyeok.fantazzk.template.CreateTemplate;
import com.naminhyeok.fantazzk.template.CreateTemplateCommand;
import com.naminhyeok.fantazzk.template.Template;
import java.util.Map;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.boot.resttestclient.TestRestTemplate;
import org.springframework.boot.resttestclient.autoconfigure.AutoConfigureTestRestTemplate;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.TestConstructor;

@SpringBootTest(
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
    properties = {
        "spring.datasource.url=jdbc:h2:mem:room-api-test;DB_CLOSE_DELAY=-1",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.datasource.username=sa",
        "spring.datasource.password=",
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "spring.liquibase.enabled=false",
        "sentry.enabled=false"
    }
)
@AutoConfigureTestRestTemplate
@TestConstructor(autowireMode = TestConstructor.AutowireMode.ALL)
@RequiredArgsConstructor
class RoomApiIntegrationTest {
    private final TestRestTemplate restTemplate;
    private final CreateTemplate createTemplate;

    @Test
    void 유효한_요청으로_방을_생성하면_201을_반환한다() {
        Template template = createAuctionTemplate();

        ResponseEntity<Map> response = restTemplate.exchange(
            RequestEntity.post("/api/v1/rooms")
                .contentType(MediaType.APPLICATION_JSON)
                .body(
                    """
                    {
                      "templateId": "%s",
                      "hostNickname": "호스트"
                    }
                    """.formatted(template.getId().templateId())
                ),
            Map.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody()).containsEntry("resultType", "SUCCESS");
        assertThat(((Map<?, ?>) response.getBody().get("success")).get("status")).isEqualTo("WAITING");
    }

    @Test
    void 존재하지_않는_템플릿으로_방을_생성하면_404를_반환한다() {
        String missingTemplateId = UUID.randomUUID().toString();

        ResponseEntity<Map> response = restTemplate.exchange(
            RequestEntity.post("/api/v1/rooms")
                .contentType(MediaType.APPLICATION_JSON)
                .body(
                    """
                    {
                      "templateId": "%s",
                      "hostNickname": "호스트"
                    }
                    """.formatted(missingTemplateId)
                ),
            Map.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody()).containsEntry("resultType", "ERROR");
        Map<?, ?> error = (Map<?, ?>) response.getBody().get("error");
        assertThat(error.get("code")).isEqualTo("ROOM_TEMPLATE_NOT_FOUND");
        assertThat(error.get("message")).isEqualTo("방 생성에 사용할 템플릿을 찾을 수 없습니다");
        assertThat(((Map<?, ?>) error.get("data")).get("templateId")).isEqualTo(missingTemplateId);
    }

    @Test
    void 존재하는_방을_조회하면_200과_방_정보를_반환한다() {
        Template template = createAuctionTemplate();
        String code = createRoom(template);

        ResponseEntity<Map> response = restTemplate.getForEntity("/api/v1/rooms/" + code, Map.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(((Map<?, ?>) response.getBody().get("success")).get("code")).isEqualTo(code);
    }

    @Test
    void 존재하지_않는_방을_조회하면_404를_반환한다() {
        String missingCode = "MISSING";

        ResponseEntity<Map> response = restTemplate.getForEntity("/api/v1/rooms/" + missingCode, Map.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody()).containsEntry("resultType", "ERROR");
        Map<?, ?> error = (Map<?, ?>) response.getBody().get("error");
        assertThat(error.get("code")).isEqualTo("ROOM_NOT_FOUND");
        assertThat(error.get("message")).isEqualTo("방을 찾을 수 없습니다");
        assertThat(((Map<?, ?>) error.get("data")).get("code")).isEqualTo(missingCode);
    }

    @Test
    void 유효한_요청으로_참가하면_200을_반환한다() {
        Template template = createAuctionTemplate();
        String code = createRoom(template);

        ResponseEntity<Map> response = restTemplate.exchange(
            RequestEntity.post("/api/v1/rooms/" + code + "/join")
                .contentType(MediaType.APPLICATION_JSON)
                .body(
                    """
                    {
                      "nickname": "게스트"
                    }
                    """
                ),
            Map.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(((Map<?, ?>) response.getBody().get("success")).get("status")).isEqualTo("WAITING");
    }

    @Test
    void 방을_시작하면_200과_진행중_상태를_반환한다() {
        Template template = createAuctionTemplate();
        String code = createRoom(template);

        restTemplate.exchange(
            RequestEntity.post("/api/v1/rooms/" + code + "/join")
                .contentType(MediaType.APPLICATION_JSON)
                .body(
                    """
                    {
                      "nickname": "게스트"
                    }
                    """
                ),
            Map.class
        );

        ResponseEntity<Map> response = restTemplate.exchange(
            RequestEntity.post("/api/v1/rooms/" + code + "/start")
                .contentType(MediaType.APPLICATION_JSON)
                .body(""),
            Map.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(((Map<?, ?>) response.getBody().get("success")).get("status")).isEqualTo("IN_PROGRESS");
    }

    @Test
    void 팀장_자리가_부족한_방은_시작할_수_없다() {
        Template template = createAuctionTemplate();
        String code = createRoom(template);

        ResponseEntity<Map> response = restTemplate.exchange(
            RequestEntity.post("/api/v1/rooms/" + code + "/start")
                .contentType(MediaType.APPLICATION_JSON)
                .body(""),
            Map.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(response.getBody()).containsEntry("resultType", "ERROR");
        Map<?, ?> error = (Map<?, ?>) response.getBody().get("error");
        assertThat(error.get("code")).isEqualTo("ROOM_NOT_STARTABLE");
        assertThat(error.get("message")).isEqualTo("방을 시작할 수 없습니다");
        assertThat(((Map<?, ?>) error.get("data")).get("detail")).isEqualTo("모든 팀장 자리가 채워져야 시작할 수 있습니다");
    }

    private Template createAuctionTemplate() {
        return createTemplate.create(
            new CreateTemplateCommand.Auction(
                "경매전",
                2,
                2,
                300,
                java.util.List.of("선수1", "선수2")
            )
        );
    }

    private String createRoom(Template template) {
        ResponseEntity<Map> response = restTemplate.exchange(
            RequestEntity.post("/api/v1/rooms")
                .contentType(MediaType.APPLICATION_JSON)
                .body(
                    """
                    {
                      "templateId": "%s",
                      "hostNickname": "호스트"
                    }
                    """.formatted(template.getId().templateId())
                ),
            Map.class
        );

        return ((Map<?, ?>) response.getBody().get("success")).get("code").toString();
    }
}
