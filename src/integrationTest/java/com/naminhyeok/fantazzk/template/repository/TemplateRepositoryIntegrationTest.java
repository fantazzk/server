package com.naminhyeok.fantazzk.template.repository;

import static org.assertj.core.api.Assertions.assertThat;

import com.naminhyeok.fantazzk.template.TemplateCatalog;
import com.naminhyeok.fantazzk.template.domain.Template;
import com.naminhyeok.fantazzk.template.domain.TemplatePlayer;
import com.naminhyeok.fantazzk.template.repository.Templates;
import jakarta.persistence.EntityManager;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.test.context.TestConstructor;
import org.springframework.transaction.annotation.Transactional;

@DataJpaTest(
    properties = {
        "spring.datasource.url=jdbc:h2:mem:template-repository-test;DB_CLOSE_DELAY=-1",
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
class TemplateRepositoryIntegrationTest {
    private final Templates templates;
    private final EntityManager entityManager;

    @Test
    @Transactional
    void 템플릿과_선수_컬렉션을_저장하고_다시_읽는다() {
        Template saved =
            templates.save(
                Template.createAuction(
                    "주말 풋살 경매전",
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
                )
            );

        entityManager.flush();
        entityManager.clear();

        Template reloaded = templates.findById(saved.getId()).orElseThrow();

        assertThat(reloaded.getId()).isEqualTo(saved.getId());
        assertThat(reloaded.getName()).isEqualTo("주말 풋살 경매전");
        assertThat(reloaded.getGameType()).isEqualTo(TemplateCatalog.GameType.LEAGUE_OF_LEGENDS);
        assertThat(reloaded.getMode()).isEqualTo(TemplateCatalog.Mode.AUCTION);
        assertThat(reloaded.getPickBanTime()).isEqualTo(45);
        assertThat(reloaded.getMinBidUnit()).isEqualTo(10);
        assertThat(reloaded.getPositionLimit()).isEqualTo(1);
        assertThat(reloaded.getPlayers())
            .extracting(TemplatePlayer::displayOrder, TemplatePlayer::name, TemplatePlayer::position)
            .containsExactly(
                org.assertj.core.groups.Tuple.tuple(0, "선수1", "TOP"),
                org.assertj.core.groups.Tuple.tuple(1, "선수2", "JUNGLE")
            );
    }
}
