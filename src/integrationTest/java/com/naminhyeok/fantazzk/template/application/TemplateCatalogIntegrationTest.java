package com.naminhyeok.fantazzk.template.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.groups.Tuple.tuple;

import com.naminhyeok.fantazzk.template.TemplateCatalog;
import com.naminhyeok.fantazzk.template.application.CreateTemplate;
import com.naminhyeok.fantazzk.template.application.CreateTemplateCommand;
import com.naminhyeok.fantazzk.template.domain.Template;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestConstructor;

@SpringBootTest(
    properties = {
        "spring.datasource.url=jdbc:h2:mem:template-catalog-test;DB_CLOSE_DELAY=-1",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.datasource.username=sa",
        "spring.datasource.password=",
        "spring.jpa.hibernate.ddl-auto=validate",
        "spring.liquibase.enabled=true",
        "sentry.enabled=false"
    }
)
@TestConstructor(autowireMode = TestConstructor.AutowireMode.ALL)
@RequiredArgsConstructor
class TemplateCatalogIntegrationTest {
    private final CreateTemplate createTemplate;
    private final TemplateCatalog templateCatalog;

    @Test
    void 템플릿_계약은_설계_정보를_외부_계약으로_변환한다() {
        Template created =
            createTemplate.create(
                new CreateTemplateCommand.Draft(
                    "사내 리그 드래프트",
                    TemplateCatalog.GameType.OVERWATCH_2,
                    2,
                    3,
                    30,
                    TemplateCatalog.DraftOrderStrategy.FIXED,
                    List.of(
                        new CreateTemplateCommand.Player("선수4", "TANK", 0),
                        new CreateTemplateCommand.Player("선수1", "SUPPORT", 1),
                        new CreateTemplateCommand.Player("선수3", "DPS", 2),
                        new CreateTemplateCommand.Player("선수2", "DPS", 3)
                    )
                )
            );

        TemplateCatalog.TemplateBlueprint blueprint = templateCatalog.getTemplate(created.getId().templateId());

        assertThat(blueprint.mode()).isEqualTo(TemplateCatalog.Mode.DRAFT);
        assertThat(blueprint.teamCount()).isEqualTo(2);
        assertThat(blueprint.teamSize()).isEqualTo(3);
        assertThat(blueprint.pickBanTime()).isEqualTo(30);
        assertThat(blueprint.minBidUnit()).isNull();
        assertThat(blueprint.positionLimit()).isNull();
        assertThat(blueprint.draftOrderStrategy()).isEqualTo(TemplateCatalog.DraftOrderStrategy.FIXED);
        assertThat(blueprint.players())
            .extracting("playerIndex", "name", "position")
            .containsExactly(
                tuple(0, "선수4", "TANK"),
                tuple(1, "선수1", "SUPPORT"),
                tuple(2, "선수3", "DPS"),
                tuple(3, "선수2", "DPS")
            );
    }

    @Test
    void 존재하지_않는_템플릿은_계약_예외로_변환한다() {
        assertThatThrownBy(() -> templateCatalog.getTemplate(UUID.randomUUID()))
            .isInstanceOf(TemplateCatalog.NotFound.class);
    }
}
