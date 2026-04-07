package com.naminhyeok.fantazzk.template;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
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
        "spring.datasource.url=jdbc:h2:mem:template-api-test;DB_CLOSE_DELAY=-1",
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
class TemplateApiIntegrationTest {
    private final TestRestTemplate restTemplate;

    @Test
    void 유효한_요청으로_템플릿을_생성하면_201을_반환한다() {
        ResponseEntity<Map> response = restTemplate.exchange(
            RequestEntity.post("/api/v1/templates")
                .contentType(MediaType.APPLICATION_JSON)
                .body(
                    """
                    {
                      "name": "경매전",
                      "mode": "AUCTION",
                      "teamCount": 2,
                      "teamSize": 2,
                      "budget": 300,
                      "playerNames": ["선수1", "선수2"]
                    }
                    """
                ),
            Map.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody()).containsEntry("resultType", "SUCCESS");
        assertThat(((Map<?, ?>) response.getBody().get("success")).get("name")).isEqualTo("경매전");
    }

    @Test
    void 드래프트_요청이_예산을_포함하면_400을_반환한다() {
        ResponseEntity<Map> response = restTemplate.exchange(
            RequestEntity.post("/api/v1/templates")
                .contentType(MediaType.APPLICATION_JSON)
                .body(
                    """
                    {
                      "name": "드래프트전",
                      "mode": "DRAFT",
                      "teamCount": 2,
                      "teamSize": 2,
                      "budget": 300,
                      "draftOrderStrategy": "SNAKE",
                      "playerNames": ["선수1", "선수2"]
                    }
                    """
                ),
            Map.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).containsEntry("resultType", "ERROR");
        Map<?, ?> error = (Map<?, ?>) response.getBody().get("error");
        assertThat(error.get("code")).isEqualTo("TEMPLATE_DRAFT_BUDGET_NOT_ALLOWED");
        assertThat(error.get("message")).isEqualTo("드래프트 템플릿에는 예산을 지정할 수 없습니다");
        assertThat(error.get("data")).isNull();
    }

    @Test
    void 요청_필드_검증에_실패하면_400과_필드_에러를_반환한다() {
        ResponseEntity<Map> response = restTemplate.exchange(
            RequestEntity.post("/api/v1/templates")
                .contentType(MediaType.APPLICATION_JSON)
                .body(
                    """
                    {
                      "name": "",
                      "mode": "AUCTION",
                      "teamCount": 0,
                      "teamSize": 0,
                      "budget": 300,
                      "playerNames": []
                    }
                    """
                ),
            Map.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).containsEntry("resultType", "ERROR");
        Map<?, ?> error = (Map<?, ?>) response.getBody().get("error");
        assertThat(error.get("code")).isEqualTo("BAD_REQUEST");
        assertThat(error.get("message")).isEqualTo("요청이 올바르지 않습니다");
        Map<?, ?> data = (Map<?, ?>) error.get("data");
        assertThat(data.get("name")).isEqualTo("템플릿 이름은 비어 있을 수 없습니다");
        assertThat(data.get("teamCount")).isEqualTo("팀 수는 1 이상이어야 합니다");
        assertThat(data.get("teamSize")).isEqualTo("팀 크기는 1 이상이어야 합니다");
        assertThat(data.get("playerNames")).isEqualTo("선수 목록은 비어 있을 수 없습니다");
    }

    @Test
    void 존재하지_않는_템플릿을_조회하면_404를_반환한다() {
        String missingId = UUID.randomUUID().toString();

        ResponseEntity<Map> response = restTemplate.getForEntity("/api/v1/templates/" + missingId, Map.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody()).containsEntry("resultType", "ERROR");
        Map<?, ?> error = (Map<?, ?>) response.getBody().get("error");
        assertThat(error.get("code")).isEqualTo("TEMPLATE_NOT_FOUND");
        assertThat(error.get("message")).isEqualTo("템플릿을 찾을 수 없습니다");
        assertThat(error.get("data")).isNull();
    }

    @Test
    void 전체_목록을_조회하면_생성된_템플릿이_포함된다() {
        restTemplate.exchange(
            RequestEntity.post("/api/v1/templates")
                .contentType(MediaType.APPLICATION_JSON)
                .body(
                    """
                    {
                      "name": "목록용 경매전",
                      "mode": "AUCTION",
                      "teamCount": 2,
                      "teamSize": 2,
                      "budget": 300,
                      "playerNames": ["선수1", "선수2"]
                    }
                    """
                ),
            Map.class
        );

        ResponseEntity<Map> response = restTemplate.getForEntity("/api/v1/templates", Map.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).containsEntry("resultType", "SUCCESS");
        List<?> templates = (List<?>) response.getBody().get("success");
        assertThat(templates).isNotEmpty();
    }
}
