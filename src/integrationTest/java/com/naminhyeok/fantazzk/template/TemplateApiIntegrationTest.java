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
        "spring.jpa.hibernate.ddl-auto=validate",
        "spring.jpa.open-in-view=false",
        "spring.liquibase.enabled=true",
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
                      "gameType": "LEAGUE_OF_LEGENDS",
                      "mode": "AUCTION",
                      "teamCount": 2,
                      "teamSize": 2,
                      "budget": 300,
                      "pickBanTime": 45,
                      "minBidUnit": 10,
                      "positionLimit": 1,
                      "players": [
                        {"name": "선수1", "position": "TOP"},
                        {"name": "선수2", "position": "JUNGLE"}
                      ]
                    }
                    """
                ),
            Map.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody()).containsEntry("resultType", "SUCCESS");
        Map<?, ?> success = (Map<?, ?>) response.getBody().get("success");
        assertThat(success.get("name")).isEqualTo("경매전");
        assertThat(success.get("gameType")).isEqualTo("LEAGUE_OF_LEGENDS");
        assertThat(success.get("pickBanTime")).isEqualTo(45);
        assertThat(success.get("minBidUnit")).isEqualTo(10);
        assertThat(success.get("positionLimit")).isEqualTo(1);
        assertThat(success.get("players")).isEqualTo(
            List.of(
                Map.of("name", "선수1", "position", "TOP", "displayOrder", 0),
                Map.of("name", "선수2", "position", "JUNGLE", "displayOrder", 1)
            )
        );
    }

    @Test
    void 상세_조회는_선수_display_order를_응답에_유지한다() {
        ResponseEntity<Map> createResponse = restTemplate.exchange(
            RequestEntity.post("/api/v1/templates")
                .contentType(MediaType.APPLICATION_JSON)
                .body(
                    """
                    {
                      "name": "상세조회용 경매전",
                      "gameType": "LEAGUE_OF_LEGENDS",
                      "mode": "AUCTION",
                      "teamCount": 2,
                      "teamSize": 2,
                      "budget": 300,
                      "pickBanTime": 45,
                      "minBidUnit": 10,
                      "positionLimit": 1,
                      "players": [
                        {"name": "선수1", "position": "TOP"},
                        {"name": "선수2", "position": "JUNGLE"}
                      ]
                    }
                    """
                ),
            Map.class
        );

        String templateId = (String) ((Map<?, ?>) createResponse.getBody().get("success")).get("id");

        ResponseEntity<Map> response = restTemplate.getForEntity("/api/v1/templates/" + templateId, Map.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).containsEntry("resultType", "SUCCESS");
        Map<?, ?> success = (Map<?, ?>) response.getBody().get("success");
        assertThat(success.get("name")).isEqualTo("상세조회용 경매전");
        assertThat(success.get("gameType")).isEqualTo("LEAGUE_OF_LEGENDS");
        assertThat(success.get("pickBanTime")).isEqualTo(45);
        assertThat(success.get("minBidUnit")).isEqualTo(10);
        assertThat(success.get("positionLimit")).isEqualTo(1);
        List<Map<String, Object>> players = (List<Map<String, Object>>) success.get("players");
        assertThat(players)
            .containsExactly(
                Map.of("name", "선수1", "position", "TOP", "displayOrder", 0),
                Map.of("name", "선수2", "position", "JUNGLE", "displayOrder", 1)
            );
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
                      "gameType": "OVERWATCH_2",
                      "mode": "DRAFT",
                      "teamCount": 2,
                      "teamSize": 2,
                      "budget": 300,
                      "pickBanTime": 30,
                      "draftOrderStrategy": "SNAKE",
                      "players": [
                        {"name": "선수1", "position": "TANK"},
                        {"name": "선수2", "position": "SUPPORT"}
                      ]
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
    void 경매_요청에_최소_입찰_단위가_없으면_400을_반환한다() {
        ResponseEntity<Map> response = restTemplate.exchange(
            RequestEntity.post("/api/v1/templates")
                .contentType(MediaType.APPLICATION_JSON)
                .body(
                    """
                    {
                      "name": "경매전",
                      "gameType": "LEAGUE_OF_LEGENDS",
                      "mode": "AUCTION",
                      "teamCount": 2,
                      "teamSize": 2,
                      "budget": 300,
                      "pickBanTime": 45,
                      "positionLimit": 1,
                      "players": [
                        {"name": "선수1", "position": "TOP"},
                        {"name": "선수2", "position": "JUNGLE"}
                      ]
                    }
                    """
                ),
            Map.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).containsEntry("resultType", "ERROR");
        Map<?, ?> error = (Map<?, ?>) response.getBody().get("error");
        assertThat(error.get("code")).isEqualTo("TEMPLATE_AUCTION_MIN_BID_UNIT_REQUIRED");
        assertThat(error.get("message")).isEqualTo("경매 템플릿에는 최소 입찰 단위가 필요합니다");
        assertThat(error.get("data")).isNull();
    }

    @Test
    void 드래프트_요청에_최소_입찰_단위가_있으면_400을_반환한다() {
        ResponseEntity<Map> response = restTemplate.exchange(
            RequestEntity.post("/api/v1/templates")
                .contentType(MediaType.APPLICATION_JSON)
                .body(
                    """
                    {
                      "name": "드래프트전",
                      "gameType": "OVERWATCH_2",
                      "mode": "DRAFT",
                      "teamCount": 2,
                      "teamSize": 2,
                      "pickBanTime": 30,
                      "minBidUnit": 10,
                      "draftOrderStrategy": "SNAKE",
                      "players": [
                        {"name": "선수1", "position": "TANK"},
                        {"name": "선수2", "position": "SUPPORT"}
                      ]
                    }
                    """
                ),
            Map.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).containsEntry("resultType", "ERROR");
        Map<?, ?> error = (Map<?, ?>) response.getBody().get("error");
        assertThat(error.get("code")).isEqualTo("TEMPLATE_DRAFT_MIN_BID_UNIT_NOT_ALLOWED");
        assertThat(error.get("message")).isEqualTo("드래프트 템플릿에는 최소 입찰 단위를 지정할 수 없습니다");
        assertThat(error.get("data")).isNull();
    }

    @Test
    void 드래프트_요청에_포지션_제한이_있으면_400을_반환한다() {
        ResponseEntity<Map> response = restTemplate.exchange(
            RequestEntity.post("/api/v1/templates")
                .contentType(MediaType.APPLICATION_JSON)
                .body(
                    """
                    {
                      "name": "드래프트전",
                      "gameType": "OVERWATCH_2",
                      "mode": "DRAFT",
                      "teamCount": 2,
                      "teamSize": 2,
                      "pickBanTime": 30,
                      "positionLimit": 1,
                      "draftOrderStrategy": "SNAKE",
                      "players": [
                        {"name": "선수1", "position": "TANK"},
                        {"name": "선수2", "position": "SUPPORT"}
                      ]
                    }
                    """
                ),
            Map.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).containsEntry("resultType", "ERROR");
        Map<?, ?> error = (Map<?, ?>) response.getBody().get("error");
        assertThat(error.get("code")).isEqualTo("TEMPLATE_DRAFT_POSITION_LIMIT_NOT_ALLOWED");
        assertThat(error.get("message")).isEqualTo("드래프트 템플릿에는 포지션 제한을 지정할 수 없습니다");
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
                      "gameType": "LEAGUE_OF_LEGENDS",
                      "mode": "AUCTION",
                      "teamCount": 0,
                      "teamSize": 0,
                      "budget": 300,
                      "pickBanTime": 0,
                      "minBidUnit": 10,
                      "positionLimit": 1,
                      "players": []
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
        assertThat(data.get("pickBanTime")).isEqualTo("픽밴 시간은 1 이상이어야 합니다");
        assertThat(data.get("players")).isEqualTo("선수 목록은 비어 있을 수 없습니다");
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
                      "gameType": "LEAGUE_OF_LEGENDS",
                      "mode": "AUCTION",
                      "teamCount": 2,
                      "teamSize": 2,
                      "budget": 300,
                      "pickBanTime": 45,
                      "minBidUnit": 10,
                      "positionLimit": 1,
                      "players": [
                        {"name": "선수1", "position": "TOP"},
                        {"name": "선수2", "position": "JUNGLE"}
                      ]
                    }
                    """
                ),
            Map.class
        );

        ResponseEntity<Map> response = restTemplate.getForEntity("/api/v1/templates", Map.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).containsEntry("resultType", "SUCCESS");
        List<Map<String, Object>> templates = (List<Map<String, Object>>) response.getBody().get("success");
        assertThat(templates).anySatisfy(template -> {
            assertThat(template.get("name")).isEqualTo("목록용 경매전");
            assertThat(template.get("gameType")).isEqualTo("LEAGUE_OF_LEGENDS");
            assertThat(template.get("pickBanTime")).isEqualTo(45);
            assertThat(template.get("minBidUnit")).isEqualTo(10);
            assertThat(template.get("positionLimit")).isEqualTo(1);
            assertThat(template.get("players")).isEqualTo(
                List.of(
                    Map.of("name", "선수1", "position", "TOP", "displayOrder", 0),
                    Map.of("name", "선수2", "position", "JUNGLE", "displayOrder", 1)
                )
            );
        });
    }
}
