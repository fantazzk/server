package com.naminhyeok.fantazzk.template;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.Map;
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
class TemplateApiIntegrationTest {
    private final TestRestTemplate restTemplate;

    TemplateApiIntegrationTest(TestRestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

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
        assertThat(((Map<?, ?>) response.getBody().get("error")).get("reason"))
            .isEqualTo("드래프트 템플릿에는 예산을 지정할 수 없습니다");
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
