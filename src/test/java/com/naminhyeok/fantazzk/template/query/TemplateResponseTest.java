package com.naminhyeok.fantazzk.template.query;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.groups.Tuple.tuple;

import com.naminhyeok.fantazzk.template.TemplateCatalog;
import com.naminhyeok.fantazzk.template.domain.Template;
import com.naminhyeok.fantazzk.template.domain.TemplatePlayer;
import com.naminhyeok.fantazzk.template.query.TemplateDetailResponse;
import com.naminhyeok.fantazzk.template.query.TemplatePlayerResponse;
import com.naminhyeok.fantazzk.template.query.TemplateSummaryResponse;
import java.util.List;
import org.junit.jupiter.api.Test;

class TemplateResponseTest {
    @Test
    void 경매_템플릿_상세_응답은_핵심_필드와_선수_표시순서를_매핑한다() {
        Template template =
            Template.createAuction(
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

        TemplateDetailResponse response = TemplateDetailResponse.from(template);

        assertThat(response.name()).isEqualTo("경매전");
        assertThat(response.gameType()).isEqualTo(TemplateCatalog.GameType.LEAGUE_OF_LEGENDS);
        assertThat(response.mode()).isEqualTo(TemplateCatalog.Mode.AUCTION);
        assertThat(response.budget()).isEqualTo(300);
        assertThat(response.pickBanTime()).isEqualTo(45);
        assertThat(response.minBidUnit()).isEqualTo(10);
        assertThat(response.positionLimit()).isEqualTo(1);
        assertThat(response.players())
            .extracting(TemplatePlayerResponse::displayOrder, TemplatePlayerResponse::name, TemplatePlayerResponse::position)
            .containsExactly(
                tuple(0, "선수1", "TOP"),
                tuple(1, "선수2", "JUNGLE")
            );
    }

    @Test
    void 드래프트_템플릿_상세_응답은_드래프트_설정을_매핑한다() {
        Template template =
            Template.createDraft(
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

        TemplateDetailResponse response = TemplateDetailResponse.from(template);

        assertThat(response.gameType()).isEqualTo(TemplateCatalog.GameType.OVERWATCH_2);
        assertThat(response.mode()).isEqualTo(TemplateCatalog.Mode.DRAFT);
        assertThat(response.budget()).isNull();
        assertThat(response.pickBanTime()).isEqualTo(30);
        assertThat(response.minBidUnit()).isNull();
        assertThat(response.positionLimit()).isNull();
        assertThat(response.draftOrderStrategy()).isEqualTo(TemplateCatalog.DraftOrderStrategy.SNAKE);
    }

    @Test
    void 템플릿_요약_응답은_목록에_필요한_필드만_노출한다() {
        Template template =
            Template.createAuction(
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

        TemplateSummaryResponse response = TemplateSummaryResponse.from(template);

        assertThat(response.id()).isEqualTo(template.getId().templateId().toString());
        assertThat(response.name()).isEqualTo("경매전");
        assertThat(response.gameType()).isEqualTo(TemplateCatalog.GameType.LEAGUE_OF_LEGENDS);
        assertThat(response.mode()).isEqualTo(TemplateCatalog.Mode.AUCTION);
        assertThat(response.teamCount()).isEqualTo(2);
        assertThat(response.teamSize()).isEqualTo(2);
    }
}
