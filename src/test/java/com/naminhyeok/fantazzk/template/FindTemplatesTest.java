package com.naminhyeok.fantazzk.template;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.groups.Tuple.tuple;

import com.naminhyeok.fantazzk.CoreException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class FindTemplatesTest {
    @Test
    void 존재하지_않는_ID로_상세_조회하면_예외가_발생한다() {
        InMemoryTemplates templates = new InMemoryTemplates();
        TemplateId missingId = new TemplateId(UUID.randomUUID());

        FindTemplates cut = new FindTemplates(templates, new InMemoryTemplateListReader(templates));

        assertThatThrownBy(() -> cut.getDetail(missingId))
            .isInstanceOf(CoreException.class)
            .satisfies(ex -> {
                CoreException coreException = (CoreException) ex;
                assertThat(coreException.getError()).isEqualTo(TemplateErrorType.TEMPLATE_NOT_FOUND);
                assertThat(coreException.getData()).isNull();
            });
    }

    @Test
    void 목록_조회는_저장된_템플릿을_반환한다() {
        InMemoryTemplates templates = new InMemoryTemplates();
        templates.save(
            Template.createAuction(
                "첫째",
                GameType.LEAGUE_OF_LEGENDS,
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
        templates.save(
            Template.createDraft(
                "둘째",
                GameType.OVERWATCH_2,
                2,
                2,
                30,
                DraftOrderStrategy.SNAKE,
                List.of(
                    new TemplatePlayer("선수1", "TANK", 0),
                    new TemplatePlayer("선수2", "SUPPORT", 1)
                )
            )
        );

        FindTemplates cut = new FindTemplates(templates, new InMemoryTemplateListReader(templates));

        assertThat(cut.list()).hasSize(2);
    }

    @Test
    void 상세_조회는_템플릿과_선수_목록을_함께_반환한다() {
        InMemoryTemplates templates = new InMemoryTemplates();
        Template template =
            Template.createAuction(
                "첫째",
                GameType.LEAGUE_OF_LEGENDS,
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
        templates.save(template);

        FindTemplates cut = new FindTemplates(templates, new InMemoryTemplateListReader(templates));
        TemplateDetail detail = cut.getDetail(template.getId());

        assertThat(detail.template().getId()).isEqualTo(template.getId());
        assertThat(detail.template().getGameType()).isEqualTo(GameType.LEAGUE_OF_LEGENDS);
        assertThat(detail.template().getPickBanTime()).isEqualTo(45);
        assertThat(detail.template().getMinBidUnit()).isEqualTo(10);
        assertThat(detail.template().getPositionLimit()).isEqualTo(1);
        assertThat(detail.players())
            .extracting(TemplatePlayer::displayOrder, TemplatePlayer::name, TemplatePlayer::position)
            .containsExactly(
                tuple(0, "선수1", "TOP"),
                tuple(1, "선수2", "JUNGLE")
            );
    }

    @Test
    void 템플릿_카탈로그는_게임과_플레이어_blueprint를_확장된_형태로_노출한다() {
        InMemoryTemplates templates = new InMemoryTemplates();
        Template template =
            Template.createAuction(
                "첫째",
                GameType.LEAGUE_OF_LEGENDS,
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
        templates.save(template);

        ProvideTemplateCatalog catalog = new ProvideTemplateCatalog(new FindTemplates(templates, new InMemoryTemplateListReader(templates)));

        TemplateCatalog.TemplateBlueprint blueprint = catalog.getTemplate(template.getId().templateId());

        assertThat(blueprint.pickBanTime()).isEqualTo(45);
        assertThat(blueprint.minBidUnit()).isEqualTo(10);
        assertThat(blueprint.positionLimit()).isEqualTo(1);
        assertThat(blueprint.players())
            .extracting("playerIndex", "name", "position")
            .containsExactly(
                tuple(0, "선수1", "TOP"),
                tuple(1, "선수2", "JUNGLE")
            );
    }
    private static final class InMemoryTemplates implements Templates {
        private final HashMap<TemplateId, Template> storage = new HashMap<>();

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

    private static final class InMemoryTemplateListReader implements TemplateListReader {
        private final InMemoryTemplates templates;

        private InMemoryTemplateListReader(InMemoryTemplates templates) {
            this.templates = templates;
        }

        @Override
        public List<TemplateDetail> list() {
            return new ArrayList<>(templates.storage.values()).stream()
                .map(template -> new TemplateDetail(template, template.getPlayers()))
                .toList();
        }
    }
}
