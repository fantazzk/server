package com.naminhyeok.fantazzk.template;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.groups.Tuple.tuple;

import java.util.List;
import org.junit.jupiter.api.Test;

class TemplateResponseTest {
    @Test
    void 경매_템플릿_응답은_핵심_필드와_선수_표시순서를_매핑한다() {
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

        TemplateResponse response = TemplateResponse.from(template);

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
    void 드래프트_템플릿_응답은_드래프트_설정을_매핑한다() {
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

        TemplateResponse response = TemplateResponse.from(template);

        assertThat(response.gameType()).isEqualTo(TemplateCatalog.GameType.OVERWATCH_2);
        assertThat(response.mode()).isEqualTo(TemplateCatalog.Mode.DRAFT);
        assertThat(response.budget()).isNull();
        assertThat(response.pickBanTime()).isEqualTo(30);
        assertThat(response.minBidUnit()).isNull();
        assertThat(response.positionLimit()).isNull();
        assertThat(response.draftOrderStrategy()).isEqualTo(TemplateCatalog.DraftOrderStrategy.SNAKE);
    }
}
