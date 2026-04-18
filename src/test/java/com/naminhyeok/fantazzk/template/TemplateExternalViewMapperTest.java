package com.naminhyeok.fantazzk.template;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.groups.Tuple.tuple;

import java.util.List;
import org.junit.jupiter.api.Test;

class TemplateExternalViewMapperTest {
    @Test
    void template_detail을_카탈로그_blueprint와_api_response로_같은_규칙으로_변환한다() {
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
        TemplateDetail detail = new TemplateDetail(template, template.getPlayers());

        TemplateCatalog.TemplateBlueprint blueprint = TemplateExternalViewMapper.toBlueprint(detail);
        TemplateResponse response = TemplateExternalViewMapper.toResponse(detail);

        assertThat(blueprint.mode()).isEqualTo(TemplateCatalog.Mode.DRAFT);
        assertThat(blueprint.teamCount()).isEqualTo(2);
        assertThat(blueprint.teamSize()).isEqualTo(2);
        assertThat(blueprint.budget()).isNull();
        assertThat(blueprint.pickBanTime()).isEqualTo(30);
        assertThat(blueprint.minBidUnit()).isNull();
        assertThat(blueprint.positionLimit()).isNull();
        assertThat(blueprint.draftOrderStrategy()).isEqualTo(TemplateCatalog.DraftOrderStrategy.SNAKE);
        assertThat(blueprint.players())
            .extracting("playerIndex", "name", "position")
            .containsExactly(
                tuple(0, "선수1", "TANK"),
                tuple(1, "선수2", "SUPPORT")
            );

        assertThat(response.id()).isEqualTo(template.getId().templateId().toString());
        assertThat(response.name()).isEqualTo("드래프트전");
        assertThat(response.gameType()).isEqualTo(TemplateCatalog.GameType.OVERWATCH_2);
        assertThat(response.mode()).isEqualTo(TemplateCatalog.Mode.DRAFT);
        assertThat(response.pickBanTime()).isEqualTo(30);
        assertThat(response.draftOrderStrategy()).isEqualTo(TemplateCatalog.DraftOrderStrategy.SNAKE);
        assertThat(response.players())
            .extracting(TemplatePlayerResponse::displayOrder, TemplatePlayerResponse::name, TemplatePlayerResponse::position)
            .containsExactly(
                tuple(0, "선수1", "TANK"),
                tuple(1, "선수2", "SUPPORT")
            );
    }
}
