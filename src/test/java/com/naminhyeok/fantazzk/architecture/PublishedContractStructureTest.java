package com.naminhyeok.fantazzk.architecture;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.lang.reflect.Modifier;
import org.junit.jupiter.api.Test;

class PublishedContractStructureTest {
    @Test
    void room_루트_계약은_public이고_대표_구현은_package_private이다() throws Exception {
        assertThat(isPublic("com.naminhyeok.fantazzk.room.RoomManagement")).isTrue();
        assertThat(isPublic("com.naminhyeok.fantazzk.room.RoomView")).isTrue();
        assertThat(isPublic("com.naminhyeok.fantazzk.room.TeamLeaderView")).isTrue();

        assertThat(isPublic("com.naminhyeok.fantazzk.room.CreateRoom")).isFalse();
        assertThat(isPublic("com.naminhyeok.fantazzk.room.GetRoom")).isFalse();
        assertThat(isPublic("com.naminhyeok.fantazzk.room.Room")).isFalse();
        assertThat(isPublic("com.naminhyeok.fantazzk.room.Rooms")).isFalse();
    }

    @Test
    void template_루트_계약은_public이고_대표_구현은_package_private이다() throws Exception {
        assertThat(isPublic("com.naminhyeok.fantazzk.template.TemplateCatalog")).isTrue();
        assertThat(isPublic("com.naminhyeok.fantazzk.template.TemplateManagement")).isTrue();
        assertThat(isPublic("com.naminhyeok.fantazzk.template.CreateTemplateInput")).isTrue();
        assertThat(isPublic("com.naminhyeok.fantazzk.template.TemplateSummaryView")).isTrue();
        assertThat(isPublic("com.naminhyeok.fantazzk.template.TemplateDetailView")).isTrue();
        assertThat(isPublic("com.naminhyeok.fantazzk.template.TemplatePlayerView")).isTrue();

        assertThat(isPublic("com.naminhyeok.fantazzk.template.CreateTemplate")).isFalse();
        assertThat(isPublic("com.naminhyeok.fantazzk.template.FindTemplates")).isFalse();
        assertThat(isPublic("com.naminhyeok.fantazzk.template.Template")).isFalse();
        assertThat(isPublic("com.naminhyeok.fantazzk.template.TemplateId")).isFalse();
        assertThat(isPublic("com.naminhyeok.fantazzk.template.Templates")).isFalse();
    }

    @Test
    void legacy_layer_packages_are_empty() {
        assertClassMissing("com.naminhyeok.fantazzk.room.application.CreateRoom");
        assertClassMissing("com.naminhyeok.fantazzk.room.domain.Room");
        assertClassMissing("com.naminhyeok.fantazzk.room.repository.Rooms");
        assertClassMissing("com.naminhyeok.fantazzk.room.api.RoomApiController");
        assertClassMissing("com.naminhyeok.fantazzk.template.application.CreateTemplate");
        assertClassMissing("com.naminhyeok.fantazzk.template.domain.Template");
        assertClassMissing("com.naminhyeok.fantazzk.template.repository.Templates");
        assertClassMissing("com.naminhyeok.fantazzk.template.api.TemplateApiController");
    }

    private boolean isPublic(String className) throws Exception {
        return Modifier.isPublic(Class.forName(className).getModifiers());
    }

    private void assertClassMissing(String className) {
        assertThatThrownBy(() -> Class.forName(className))
            .isInstanceOf(ClassNotFoundException.class);
    }
}
