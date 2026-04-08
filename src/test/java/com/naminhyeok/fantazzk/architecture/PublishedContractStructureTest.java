package com.naminhyeok.fantazzk.architecture;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.lang.reflect.Modifier;
import org.junit.jupiter.api.Test;

class PublishedContractStructureTest {
    @Test
    void room_루트_계약은_public이고_대표_구현은_package_private이다() throws Exception {
        assertClassMissing("com.naminhyeok.fantazzk.room.RoomManagement");
        assertClassMissing("com.naminhyeok.fantazzk.room.RoomView");
        assertClassMissing("com.naminhyeok.fantazzk.room.TeamLeaderView");
        assertThat(isPublic("com.naminhyeok.fantazzk.room.RoomApiController")).isFalse();
        assertThat(isPublic("com.naminhyeok.fantazzk.room.CreateRoom")).isFalse();
        assertThat(isPublic("com.naminhyeok.fantazzk.room.GetRoom")).isFalse();
        assertThat(isPublic("com.naminhyeok.fantazzk.room.Room")).isFalse();
        assertThat(isPublic("com.naminhyeok.fantazzk.room.Rooms")).isFalse();
    }

    @Test
    void template_루트_계약은_public이고_대표_구현은_package_private이다() throws Exception {
        assertThat(isPublic("com.naminhyeok.fantazzk.template.TemplateCatalog")).isTrue();
        assertClassMissing("com.naminhyeok.fantazzk.template.TemplateManagement");
        assertClassMissing("com.naminhyeok.fantazzk.template.CreateTemplateInput");
        assertClassMissing("com.naminhyeok.fantazzk.template.TemplateSummaryView");
        assertClassMissing("com.naminhyeok.fantazzk.template.TemplateDetailView");
        assertClassMissing("com.naminhyeok.fantazzk.template.TemplatePlayerView");

        assertThat(isPublic("com.naminhyeok.fantazzk.template.CreateTemplate")).isFalse();
        assertThat(isPublic("com.naminhyeok.fantazzk.template.FindTemplates")).isFalse();
        assertThat(isPublic("com.naminhyeok.fantazzk.template.Template")).isFalse();
        assertThat(isPublic("com.naminhyeok.fantazzk.template.TemplateId")).isFalse();
        assertThat(isPublic("com.naminhyeok.fantazzk.template.Templates")).isFalse();
        assertThat(isPublic("com.naminhyeok.fantazzk.template.TemplateApiController")).isFalse();
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
        assertClassMissing("com.naminhyeok.fantazzk.template.contract.TemplateCatalog");
    }

    private boolean isPublic(String className) throws Exception {
        return Modifier.isPublic(Class.forName(className).getModifiers());
    }

    private void assertClassMissing(String className) {
        assertThatThrownBy(() -> Class.forName(className))
            .isInstanceOf(ClassNotFoundException.class);
    }
}
