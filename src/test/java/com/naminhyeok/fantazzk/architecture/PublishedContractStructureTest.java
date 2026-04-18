package com.naminhyeok.fantazzk.architecture;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.time.Instant;
import org.junit.jupiter.api.Test;

class PublishedContractStructureTest {
    @Test
    void room_루트_계약은_public이고_대표_구현은_package_private이다() throws Exception {
        assertClassMissing("com.naminhyeok.fantazzk.room.RoomManagement");
        assertClassMissing("com.naminhyeok.fantazzk.room.RoomView");
        assertClassMissing("com.naminhyeok.fantazzk.room.TeamLeaderView");
        assertClassMissing("com.naminhyeok.fantazzk.room.RoomApiController");
        assertThat(isPublic("com.naminhyeok.fantazzk.room.RoomQueryApiController")).isFalse();
        assertThat(isPublic("com.naminhyeok.fantazzk.room.RoomSessionApiController")).isFalse();
        assertThat(isPublic("com.naminhyeok.fantazzk.room.RoomStartApiController")).isFalse();
        assertThat(isPublic("com.naminhyeok.fantazzk.room.RoomDraftApiController")).isFalse();
        assertThat(isPublic("com.naminhyeok.fantazzk.room.CreateRoom")).isFalse();
        assertThat(isPublic("com.naminhyeok.fantazzk.room.GetRoom")).isFalse();
        assertThat(isPublic("com.naminhyeok.fantazzk.room.Room")).isFalse();
        assertThat(isPublic("com.naminhyeok.fantazzk.room.Rooms")).isFalse();
    }

    @Test
    void template_루트_계약은_public이고_대표_구현은_package_private이다() throws Exception {
        assertThat(isPublic("com.naminhyeok.fantazzk.template.TemplateCatalog")).isTrue();
        assertThat(isPublic("com.naminhyeok.fantazzk.template.TemplateConfiguration")).isFalse();
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
        assertClassMissing("com.naminhyeok.fantazzk.room.GetRoomDetails");
        assertClassMissing("com.naminhyeok.fantazzk.template.application.CreateTemplate");
        assertClassMissing("com.naminhyeok.fantazzk.template.domain.Template");
        assertClassMissing("com.naminhyeok.fantazzk.template.repository.Templates");
        assertClassMissing("com.naminhyeok.fantazzk.template.api.TemplateApiController");
        assertClassMissing("com.naminhyeok.fantazzk.template.contract.TemplateCatalog");
    }

    @Test
    void aggregate_repository는_별도_jpa_seam_클래스를_두지_않는다() {
        assertClassMissing("com.naminhyeok.fantazzk.room.JpaRoomRepository");
        assertClassMissing("com.naminhyeok.fantazzk.room.JpaRooms");
        assertClassMissing("com.naminhyeok.fantazzk.template.JpaTemplateRepository");
        assertClassMissing("com.naminhyeok.fantazzk.template.JpaTemplates");
    }

    @Test
    void 사용하지_않는_room_domain_event는_남기지_않는다() {
        assertClassMissing("com.naminhyeok.fantazzk.room.event.LeaderJoinedRoom");
    }

    @Test
    void room_aggregate는_준비_행위만_가지고_started_game_live_play를_직접_소유하지_않는다() throws Exception {
        Class<?> teamLeaderId = Class.forName("com.naminhyeok.fantazzk.room.TeamLeaderId");

        assertThat(hasDeclaredMethod("start", teamLeaderId)).isFalse();
        assertThat(hasDeclaredMethod("placeBid", teamLeaderId, int.class)).isFalse();
        assertThat(hasDeclaredMethod("settleAuction")).isFalse();
        assertThat(hasDeclaredMethod("start", teamLeaderId, Instant.class)).isTrue();
        assertThat(hasDeclaredMethod("placeBid", teamLeaderId, int.class, Instant.class)).isFalse();
        assertThat(hasDeclaredMethod("settleAuction", Instant.class)).isFalse();
    }

    private boolean isPublic(String className) throws Exception {
        return Modifier.isPublic(Class.forName(className).getModifiers());
    }

    private void assertClassMissing(String className) {
        assertThatThrownBy(() -> Class.forName(className))
            .isInstanceOf(ClassNotFoundException.class);
    }

    private boolean hasDeclaredMethod(String name, Class<?>... parameterTypes) {
        try {
            Method ignored = Class.forName("com.naminhyeok.fantazzk.room.Room").getDeclaredMethod(name, parameterTypes);
            return true;
        } catch (NoSuchMethodException ex) {
            return false;
        } catch (ClassNotFoundException ex) {
            throw new AssertionError(ex);
        }
    }
}
