package com.naminhyeok.fantazzk.template;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.groups.Tuple.tuple;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.Test;

class CreateTemplateTest {
    @Test
    void 경매_생성_command로_템플릿과_선수_컬렉션을_저장한다() {
        InMemoryTemplates templates = new InMemoryTemplates();

        CreateTemplate cut = new CreateTemplate(templates);

        Template template =
            cut.create(
                new CreateTemplateCommand.Auction(
                    "경매전",
                    2,
                    2,
                    500,
                    List.of("선수A", "선수B")
                )
            );

        assertThat(template.getName()).isEqualTo("경매전");
        assertThat(template.getConfiguration()).isEqualTo(TemplateConfiguration.auction(2, 2, 500));
        assertThat(template.getPlayers())
            .extracting("playerIndex", "name")
            .containsExactly(
                tuple(0, "선수A"),
                tuple(1, "선수B")
            );
    }

    @Test
    void 드래프트_생성_command로_드래프트_설정을_저장한다() {
        InMemoryTemplates templates = new InMemoryTemplates();

        CreateTemplate cut = new CreateTemplate(templates);

        Template template =
            cut.create(
                new CreateTemplateCommand.Draft(
                    "드래프트전",
                    2,
                    2,
                    DraftOrderStrategy.SNAKE,
                    List.of("선수1", "선수2")
                )
            );

        assertThat(template.getConfiguration()).isEqualTo(TemplateConfiguration.draft(2, 2, DraftOrderStrategy.SNAKE));
        assertThat(template.getBudget()).isNull();
    }

    @Test
    void 서비스는_선수_수를_정확히_강제한다() {
        CreateTemplate cut = new CreateTemplate(new InMemoryTemplates());

        assertThatThrownBy(() ->
            cut.create(
                new CreateTemplateCommand.Auction(
                    "실패",
                    2,
                    2,
                    300,
                    List.of("선수1")
                )
            )
        )
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("선수 수는 정확히 2명이어야 합니다");
    }

    private static final class InMemoryTemplates implements Templates {
        private final Map<TemplateId, Template> storage = new HashMap<>();

        @Override
        public Template save(Template template) {
            storage.put(template.getId(), template);
            return template;
        }

        @Override
        public Optional<Template> findById(TemplateId id) {
            return Optional.ofNullable(storage.get(id));
        }

        @Override
        public List<Template> findAll() {
            return new ArrayList<>(storage.values());
        }
    }
}
