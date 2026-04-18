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
    void 경매_생성_command로_확장된_설정과_타입드_선수_컬렉션을_저장한다() {
        InMemoryTemplates templates = new InMemoryTemplates();

        CreateTemplate cut = new CreateTemplate(templates);

        Template template =
            cut.create(
                new CreateTemplateCommand.Auction(
                    "경매전",
                    TemplateCatalog.GameType.LEAGUE_OF_LEGENDS,
                    2,
                    2,
                    500,
                    45,
                    10,
                    1,
                    List.of(
                        new CreateTemplateCommand.Player("선수A", "TOP", 0),
                        new CreateTemplateCommand.Player("선수B", "JUNGLE", 1)
                    )
                )
            );

        assertThat(template.getName()).isEqualTo("경매전");
        assertThat(template.getConfiguration())
            .isEqualTo(TemplateConfiguration.auction(TemplateCatalog.GameType.LEAGUE_OF_LEGENDS, 2, 2, 500, 45, 10, 1));
        assertThat(template.getPlayers())
            .extracting(TemplatePlayer::displayOrder, TemplatePlayer::name, TemplatePlayer::position)
            .containsExactly(
                tuple(0, "선수A", "TOP"),
                tuple(1, "선수B", "JUNGLE")
            );
    }

    @Test
    void 드래프트_생성_command로_게임타입과_픽밴시간을_포함한_설정을_저장한다() {
        InMemoryTemplates templates = new InMemoryTemplates();

        CreateTemplate cut = new CreateTemplate(templates);

        Template template =
            cut.create(
                new CreateTemplateCommand.Draft(
                    "드래프트전",
                    TemplateCatalog.GameType.OVERWATCH_2,
                    2,
                    2,
                    30,
                    TemplateCatalog.DraftOrderStrategy.SNAKE,
                    List.of(
                        new CreateTemplateCommand.Player("선수1", "TANK", 0),
                        new CreateTemplateCommand.Player("선수2", "SUPPORT", 1)
                    )
                )
            );

        assertThat(template.getConfiguration())
            .isEqualTo(TemplateConfiguration.draft(TemplateCatalog.GameType.OVERWATCH_2, 2, 2, 30, TemplateCatalog.DraftOrderStrategy.SNAKE));
        assertThat(template.getBudget()).isNull();
    }

    @Test
    void 서비스는_선수_수를_정확히_강제한다() {
        CreateTemplate cut = new CreateTemplate(new InMemoryTemplates());

        assertThatThrownBy(() ->
            cut.create(
                new CreateTemplateCommand.Auction(
                    "실패",
                    TemplateCatalog.GameType.LEAGUE_OF_LEGENDS,
                    2,
                    2,
                    300,
                    45,
                    10,
                    1,
                    List.of(new CreateTemplateCommand.Player("선수1", "TOP", 0))
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
    }
}
