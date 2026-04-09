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

        FindTemplates cut = new FindTemplates(templates);

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
        templates.save(Template.createAuction("첫째", 2, 2, 300, List.of("선수1", "선수2")));
        templates.save(Template.createDraft("둘째", 2, 2, DraftOrderStrategy.SNAKE, List.of("선수1", "선수2")));

        FindTemplates cut = new FindTemplates(templates);

        assertThat(cut.list()).hasSize(2);
    }

    @Test
    void 상세_조회는_템플릿과_선수_목록을_함께_반환한다() {
        InMemoryTemplates templates = new InMemoryTemplates();
        Template template = Template.createAuction("첫째", 2, 2, 300, List.of("선수1", "선수2"));
        templates.save(template);

        FindTemplates cut = new FindTemplates(templates);
        TemplateDetail detail = cut.getDetail(template.getId());

        assertThat(detail.template().getId()).isEqualTo(template.getId());
        assertThat(detail.players())
            .extracting("playerIndex", "name")
            .containsExactly(
                tuple(0, "선수1"),
                tuple(1, "선수2")
            );
    }

    @Test
    void 템플릿_카탈로그는_플레이어_blueprint에_aggregate_local_player_index를_노출한다() {
        InMemoryTemplates templates = new InMemoryTemplates();
        Template template = Template.createAuction("첫째", 2, 2, 300, List.of("선수1", "선수2"));
        templates.save(template);

        ProvideTemplateCatalog catalog = new ProvideTemplateCatalog(new FindTemplates(templates));

        TemplateCatalog.TemplateBlueprint blueprint = catalog.getTemplate(template.getId().templateId());

        assertThat(blueprint.players())
            .extracting("playerIndex", "name")
            .containsExactly(
                tuple(0, "선수1"),
                tuple(1, "선수2")
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

        @Override
        public List<Template> findAll() {
            return new ArrayList<>(storage.values());
        }
    }
}
