package com.naminhyeok.fantazzk.template;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class FindTemplatesTest {
    @Mock
    private Templates templates;

    @Test
    void 존재하지_않는_ID로_상세_조회하면_예외가_발생한다() {
        Template.TemplateId missingId = new Template.TemplateId(UUID.randomUUID());
        when(templates.findById(missingId)).thenReturn(Optional.empty());

        FindTemplates cut = new FindTemplates(templates);

        assertThatThrownBy(() -> cut.getDetail(missingId))
            .isInstanceOf(TemplateNotFoundException.class);
    }

    @Test
    void 목록_조회는_저장된_템플릿을_반환한다() {
        when(templates.findAll()).thenReturn(
            List.of(
                Template.createAuction("첫째", 2, 2, 300, List.of("선수1", "선수2")),
                Template.createDraft("둘째", 2, 2, DraftOrderStrategy.SNAKE, List.of("선수1", "선수2"))
            )
        );

        FindTemplates cut = new FindTemplates(templates);

        assertThat(cut.list()).hasSize(2);
    }

    @Test
    void 상세_조회는_템플릿과_선수_목록을_함께_반환한다() {
        Template template = Template.createAuction("첫째", 2, 2, 300, List.of("선수1", "선수2"));
        when(templates.findById(template.getId())).thenReturn(Optional.of(template));

        FindTemplates cut = new FindTemplates(templates);
        TemplateDetail detail = cut.getDetail(template.getId());

        assertThat(detail.template().getId()).isEqualTo(template.getId());
        assertThat(detail.players().stream().map(TemplatePlayer::getName))
            .containsExactly("선수1", "선수2");
    }
}
