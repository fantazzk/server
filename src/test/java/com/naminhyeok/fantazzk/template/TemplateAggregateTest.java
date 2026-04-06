package com.naminhyeok.fantazzk.template;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.naminhyeok.fantazzk.template.domain.DraftOrderStrategy;
import com.naminhyeok.fantazzk.template.domain.TeamBuildingMode;
import com.naminhyeok.fantazzk.template.domain.Template;
import com.naminhyeok.fantazzk.template.domain.TemplateConfiguration;
import com.naminhyeok.fantazzk.template.domain.TemplatePlayer;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class TemplateAggregateTest {

    @Test
    void createAuctionValidatesRosterSize() {
        assertThatThrownBy(() ->
                Template.createAuction("auction", 2, 2, 300, List.of("선수1")))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("선수 수는 정확히 2명이어야 합니다");
    }

    @Test
    void createDraftExposesTypedRootIdConfigurationAndImmutablePlayers() {
        TemplateId templateId = TemplateId.from(UUID.randomUUID());

        Template template = Template.createDraft(
                "draft",
                2,
                2,
                DraftOrderStrategy.SNAKE,
                List.of("선수1", "선수2"))
            .assignId(templateId);

        assertThat(template.getId()).isEqualTo(templateId);
        assertThat(template.getTemplateId()).isEqualTo(templateId);
        assertThat(template.getMode()).isEqualTo(TeamBuildingMode.DRAFT);
        assertThat(template.getConfiguration())
            .isEqualTo(TemplateConfiguration.draft(2, 2, DraftOrderStrategy.SNAKE));
        assertThat(template.players())
            .extracting(TemplatePlayer::getName, TemplatePlayer::getDisplayOrder)
            .containsExactly(
                org.assertj.core.groups.Tuple.tuple("선수1", 0),
                org.assertj.core.groups.Tuple.tuple("선수2", 1));
        assertThatThrownBy(() -> template.players().add(new TemplatePlayer("추가선수", 2)))
            .isInstanceOf(UnsupportedOperationException.class);
    }
}
