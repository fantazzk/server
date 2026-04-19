package com.naminhyeok.fantazzk.template.web;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

import com.naminhyeok.fantazzk.GlobalExceptionHandler;
import com.naminhyeok.fantazzk.template.TemplateCatalog;
import com.naminhyeok.fantazzk.template.application.CreateTemplate;
import com.naminhyeok.fantazzk.template.domain.Template;
import com.naminhyeok.fantazzk.template.domain.TemplatePlayer;
import com.naminhyeok.fantazzk.template.query.FindTemplates;
import com.naminhyeok.fantazzk.template.query.TemplateDetail;
import com.naminhyeok.fantazzk.template.web.TemplateApiController;
import java.util.List;
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

@WebMvcTest(TemplateApiController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import(GlobalExceptionHandler.class)
class TemplateApiControllerWebMvcTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private CreateTemplate createTemplate;

    @MockitoBean
    private FindTemplates findTemplates;

    @Test
    void list는_템플릿_요약_목록만_반환한다() throws Exception {
        Template template = auctionTemplate();
        given(findTemplates.list()).willReturn(List.of(new TemplateDetail(template, template.getPlayers())));

        var result = mockMvcTester().perform(get("/api/v1/templates"));

        result.assertThat().hasStatusOk();
        JsonNode body = objectMapper.readTree(result.getResponse().getContentAsString());
        assertThat(body.at("/resultType").asText()).isEqualTo("SUCCESS");
        assertThat(body.at("/success/0/id").asText()).isEqualTo(template.getId().templateId().toString());
        assertThat(body.at("/success/0/name").asText()).isEqualTo("경매전");
        assertThat(body.at("/success/0/teamCount").asInt()).isEqualTo(2);
        assertThat(body.at("/success/0/players").isMissingNode()).isTrue();
        assertThat(body.at("/success/0/pickBanTime").isMissingNode()).isTrue();
    }

    @Test
    void getById는_템플릿_상세를_반환한다() throws Exception {
        Template template = draftTemplate();
        given(findTemplates.getDetail(template.getId())).willReturn(new TemplateDetail(template, template.getPlayers()));

        var result = mockMvcTester().perform(get("/api/v1/templates/{id}", template.getId().templateId()));

        result.assertThat().hasStatusOk();
        JsonNode body = objectMapper.readTree(result.getResponse().getContentAsString());
        assertThat(body.at("/resultType").asText()).isEqualTo("SUCCESS");
        assertThat(body.at("/success/id").asText()).isEqualTo(template.getId().templateId().toString());
        assertThat(body.at("/success/name").asText()).isEqualTo("드래프트전");
        assertThat(body.at("/success/draftOrderStrategy").asText()).isEqualTo("SNAKE");
        assertThat(body.at("/success/players/0/name").asText()).isEqualTo("선수1");
    }

    @Test
    void create는_생성된_템플릿_상세를_반환한다() throws Exception {
        Template template = auctionTemplate();
        given(createTemplate.create(any())).willReturn(template);

        var result = mockMvcTester().perform(
            post("/api/v1/templates")
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    """
                    {
                      "name": "경매전",
                      "gameType": "LEAGUE_OF_LEGENDS",
                      "mode": "AUCTION",
                      "teamCount": 2,
                      "teamSize": 2,
                      "pickBanTime": 45,
                      "budget": 300,
                      "minBidUnit": 10,
                      "positionLimit": 1,
                      "players": [
                        { "name": "선수1", "position": "TOP" },
                        { "name": "선수2", "position": "JUNGLE" }
                      ]
                    }
                    """
                )
        );

        result.assertThat().hasStatus(HttpStatus.CREATED);
        JsonNode body = objectMapper.readTree(result.getResponse().getContentAsString());
        assertThat(body.at("/resultType").asText()).isEqualTo("SUCCESS");
        assertThat(body.at("/success/id").asText()).isEqualTo(template.getId().templateId().toString());
        assertThat(body.at("/success/players/1/position").asText()).isEqualTo("JUNGLE");
    }

    private MockMvcTester mockMvcTester() {
        return MockMvcTester.create(mockMvc);
    }

    private Template auctionTemplate() {
        return Template.createAuction(
            "경매전",
            TemplateCatalog.GameType.LEAGUE_OF_LEGENDS,
            2,
            2,
            300,
            45,
            10,
            1,
            List.of(
                new TemplatePlayer("선수1", "TOP", 0),
                new TemplatePlayer("선수2", "JUNGLE", 1)
            )
        );
    }

    private Template draftTemplate() {
        return Template.createDraft(
            "드래프트전",
            TemplateCatalog.GameType.OVERWATCH_2,
            2,
            2,
            30,
            TemplateCatalog.DraftOrderStrategy.SNAKE,
            List.of(
                new TemplatePlayer("선수1", "TANK", 0),
                new TemplatePlayer("선수2", "SUPPORT", 1)
            )
        );
    }
}
