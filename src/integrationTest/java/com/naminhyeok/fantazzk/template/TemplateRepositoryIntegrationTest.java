package com.naminhyeok.fantazzk.template;

import static org.assertj.core.api.Assertions.assertThat;

import jakarta.persistence.EntityManager;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestConstructor;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest(
    properties = {
        "spring.datasource.url=jdbc:h2:mem:template-repository-test;DB_CLOSE_DELAY=-1",
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
                    2,
                    2,
                    300,
                    List.of("선수1", "선수2")
                )
            );

        entityManager.flush();
        entityManager.clear();

        Template reloaded = templates.findById(saved.getId()).orElseThrow();

        assertThat(reloaded.getId()).isEqualTo(saved.getId());
        assertThat(reloaded.getName()).isEqualTo("주말 풋살 경매전");
        assertThat(reloaded.getMode()).isEqualTo(TemplateMode.AUCTION);
        assertThat(reloaded.getPlayers().stream().map(TemplatePlayer::getName))
            .containsExactly("선수1", "선수2");
    }
}
