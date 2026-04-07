package com.naminhyeok.fantazzk.template;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

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
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "spring.liquibase.enabled=false",
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
                    2,
                    3,
                    DraftOrderStrategy.FIXED,
                    List.of("선수4", "선수1", "선수3", "선수2")
                )
            );

        TemplateCatalog.TemplateBlueprint blueprint = templateCatalog.getTemplate(created.getId());

        assertThat(blueprint.mode()).isEqualTo(TemplateMode.DRAFT);
        assertThat(blueprint.teamCount()).isEqualTo(2);
        assertThat(blueprint.teamSize()).isEqualTo(3);
        assertThat(blueprint.draftOrderStrategy()).isEqualTo(DraftOrderStrategy.FIXED);
        assertThat(blueprint.players().stream().map(TemplateCatalog.TemplatePlayerBlueprint::name))
            .containsExactly("선수4", "선수1", "선수3", "선수2");
    }

    @Test
    void 존재하지_않는_템플릿은_계약_예외로_변환한다() {
        assertThatThrownBy(() -> templateCatalog.getTemplate(new TemplateId(UUID.randomUUID())))
            .isInstanceOf(TemplateCatalogException.NotFound.class);
    }
}
