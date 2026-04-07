package com.naminhyeok.fantazzk.template;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class CreateTemplateTest {
    @Mock
    private Templates templates;

    @Test
    void 경매_생성_command로_템플릿과_선수_컬렉션을_저장한다() {
        when(templates.save(any(Template.class))).thenAnswer(invocation -> invocation.getArgument(0));

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
        assertThat(template.getPlayers().stream().map(TemplatePlayer::getName))
            .containsExactly("선수A", "선수B");
    }

    @Test
    void 드래프트_생성_command로_드래프트_설정을_저장한다() {
        when(templates.save(any(Template.class))).thenAnswer(invocation -> invocation.getArgument(0));

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
        CreateTemplate cut = new CreateTemplate(templates);

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
}
