package com.naminhyeok.fantazzk.architecture;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.naminhyeok.fantazzk.room.application.CreateRoom;
import com.naminhyeok.fantazzk.room.domain.AuctionSettled;
import com.naminhyeok.fantazzk.room.domain.BidPlaced;
import com.naminhyeok.fantazzk.room.domain.Room;
import com.naminhyeok.fantazzk.room.domain.RoomSchedulingEvent;
import com.naminhyeok.fantazzk.room.domain.RoomStarted;
import com.naminhyeok.fantazzk.room.domain.TeamLeaderId;
import com.naminhyeok.fantazzk.room.infrastructure.realtime.SupabaseRoomRealtimePublisher;
import com.naminhyeok.fantazzk.room.query.GetRoom;
import com.naminhyeok.fantazzk.room.repository.Rooms;
import com.naminhyeok.fantazzk.room.web.RoomDraftApiController;
import com.naminhyeok.fantazzk.room.web.RoomQueryApiController;
import com.naminhyeok.fantazzk.room.web.RoomSessionApiController;
import com.naminhyeok.fantazzk.room.web.RoomStartApiController;
import com.naminhyeok.fantazzk.template.TemplateCatalog;
import com.naminhyeok.fantazzk.template.application.CreateTemplate;
import com.naminhyeok.fantazzk.template.domain.Template;
import com.naminhyeok.fantazzk.template.domain.TemplateId;
import com.naminhyeok.fantazzk.template.infrastructure.persistence.JpaTemplateListReader;
import com.naminhyeok.fantazzk.template.query.FindTemplates;
import com.naminhyeok.fantazzk.template.repository.Templates;
import com.naminhyeok.fantazzk.template.web.TemplateApiController;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.time.Instant;
import org.junit.jupiter.api.Test;

class PublishedContractStructureTest {
    @Test
    void room_모듈은_역할별_내부_패키지로_구성한다() throws Exception {
        assertClassMissing("com.naminhyeok.fantazzk.room.RoomManagement");
        assertClassMissing("com.naminhyeok.fantazzk.room.RoomView");
        assertClassMissing("com.naminhyeok.fantazzk.room.TeamLeaderView");
        assertClassMissing("com.naminhyeok.fantazzk.room.RoomApiController");
        assertClassMissing("com.naminhyeok.fantazzk.room.RoomQueryApiController");
        assertClassMissing("com.naminhyeok.fantazzk.room.RoomSessionApiController");
        assertClassMissing("com.naminhyeok.fantazzk.room.RoomStartApiController");
        assertClassMissing("com.naminhyeok.fantazzk.room.RoomDraftApiController");
        assertClassMissing("com.naminhyeok.fantazzk.room.CreateRoom");
        assertClassMissing("com.naminhyeok.fantazzk.room.GetRoom");
        assertClassMissing("com.naminhyeok.fantazzk.room.Room");
        assertClassMissing("com.naminhyeok.fantazzk.room.Rooms");

        assertThat(isPublic("com.naminhyeok.fantazzk.room.domain.Room")).isTrue();
        assertThat(isPublic("com.naminhyeok.fantazzk.room.application.CreateRoom")).isTrue();
        assertThat(isPublic("com.naminhyeok.fantazzk.room.query.GetRoom")).isTrue();
        assertThat(isPublic("com.naminhyeok.fantazzk.room.repository.Rooms")).isTrue();
        assertThat(isPublic("com.naminhyeok.fantazzk.room.web.RoomQueryApiController")).isTrue();
        assertThat(isPublic("com.naminhyeok.fantazzk.room.infrastructure.realtime.SupabaseRoomRealtimePublisher")).isTrue();
    }

    @Test
    void template_모듈은_역할별_내부_패키지로_구성한다() throws Exception {
        assertThat(isPublic("com.naminhyeok.fantazzk.template.TemplateCatalog")).isTrue();
        assertClassMissing("com.naminhyeok.fantazzk.template.TemplateManagement");
        assertClassMissing("com.naminhyeok.fantazzk.template.CreateTemplateInput");
        assertClassMissing("com.naminhyeok.fantazzk.template.TemplateSummaryView");
        assertClassMissing("com.naminhyeok.fantazzk.template.TemplateDetailView");
        assertClassMissing("com.naminhyeok.fantazzk.template.TemplatePlayerView");

        assertClassMissing("com.naminhyeok.fantazzk.template.CreateTemplate");
        assertClassMissing("com.naminhyeok.fantazzk.template.FindTemplates");
        assertClassMissing("com.naminhyeok.fantazzk.template.Template");
        assertClassMissing("com.naminhyeok.fantazzk.template.TemplateId");
        assertClassMissing("com.naminhyeok.fantazzk.template.Templates");
        assertClassMissing("com.naminhyeok.fantazzk.template.TemplateApiController");

        assertThat(isPublic("com.naminhyeok.fantazzk.template.domain.Template")).isTrue();
        assertThat(isPublic("com.naminhyeok.fantazzk.template.application.CreateTemplate")).isTrue();
        assertThat(isPublic("com.naminhyeok.fantazzk.template.query.FindTemplates")).isTrue();
        assertThat(isPublic("com.naminhyeok.fantazzk.template.repository.Templates")).isTrue();
        assertThat(isPublic("com.naminhyeok.fantazzk.template.web.TemplateApiController")).isTrue();
        assertThat(isPublic("com.naminhyeok.fantazzk.template.infrastructure.persistence.JpaTemplateListReader")).isTrue();
    }

    @Test
    void template은_legacy_layered_name을_남기지_않는다() {
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
    void 사용하지_않는_room_domain_event는_남기지_않는다() throws Exception {
        assertClassMissing("com.naminhyeok.fantazzk.room.event.LeaderJoinedRoom");
        assertClassMissing("com.naminhyeok.fantazzk.room.event.RoomSchedulingEvent");
        assertClassMissing("com.naminhyeok.fantazzk.room.event.RoomStarted");
        assertClassMissing("com.naminhyeok.fantazzk.room.event.BidPlaced");
        assertClassMissing("com.naminhyeok.fantazzk.room.event.AuctionSettled");
        assertThat(isPublic("com.naminhyeok.fantazzk.room.domain.RoomSchedulingEvent")).isTrue();
        assertThat(isPublic("com.naminhyeok.fantazzk.room.domain.RoomStarted")).isTrue();
        assertThat(isPublic("com.naminhyeok.fantazzk.room.domain.BidPlaced")).isTrue();
        assertThat(isPublic("com.naminhyeok.fantazzk.room.domain.AuctionSettled")).isTrue();
    }

    @Test
    void room_aggregate는_준비_행위만_가지고_started_game_live_play를_직접_소유하지_않는다() throws Exception {
        Class<?> teamLeaderId = Class.forName("com.naminhyeok.fantazzk.room.domain.TeamLeaderId");

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
            Method ignored = Class.forName("com.naminhyeok.fantazzk.room.domain.Room").getDeclaredMethod(name, parameterTypes);
            return true;
        } catch (NoSuchMethodException ex) {
            return false;
        } catch (ClassNotFoundException ex) {
            throw new AssertionError(ex);
        }
    }
}
