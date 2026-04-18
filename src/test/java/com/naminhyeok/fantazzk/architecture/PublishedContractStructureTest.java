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
        assertClassMissing("com.naminhyeok.fantazzk.room.JoinableRoomResponse");
        assertClassMissing("com.naminhyeok.fantazzk.room.RoomViewResponse");
        assertClassMissing("com.naminhyeok.fantazzk.room.RoomSessionResponse");
        assertClassMissing("com.naminhyeok.fantazzk.room.GameResponse");
        assertClassMissing("com.naminhyeok.fantazzk.room.RoomApiController");
        assertThat(isPublic("com.naminhyeok.fantazzk.room.RoomQueryApi")).isTrue();
        assertThat(isPublic("com.naminhyeok.fantazzk.room.RoomSessionApi")).isTrue();
        assertThat(isPublic("com.naminhyeok.fantazzk.room.RoomDraftApi")).isTrue();
        assertThat(isPublic("com.naminhyeok.fantazzk.room.RoomStartApi")).isTrue();
        assertThat(isPublic("com.naminhyeok.fantazzk.room.GameQueryApi")).isTrue();
        assertThat(isPublic("com.naminhyeok.fantazzk.room.GameAuctionApi")).isTrue();
        assertThat(isPublic("com.naminhyeok.fantazzk.room.GameDraftApi")).isTrue();
        assertThat(isPublic("com.naminhyeok.fantazzk.room.JoinableRoomView")).isTrue();
        assertThat(isPublic("com.naminhyeok.fantazzk.room.RoomView")).isTrue();
        assertThat(isPublic("com.naminhyeok.fantazzk.room.RoomSessionView")).isTrue();
        assertThat(isPublic("com.naminhyeok.fantazzk.room.GameView")).isTrue();
        assertThat(isPublic("com.naminhyeok.fantazzk.room.ProvideRoomQueryApi")).isFalse();
        assertThat(isPublic("com.naminhyeok.fantazzk.room.ProvideRoomSessionApi")).isFalse();
        assertThat(isPublic("com.naminhyeok.fantazzk.room.ProvideRoomDraftApi")).isFalse();
        assertThat(isPublic("com.naminhyeok.fantazzk.room.ProvideRoomStartApi")).isFalse();
        assertThat(isPublic("com.naminhyeok.fantazzk.room.ProvideGameQueryApi")).isFalse();
        assertThat(isPublic("com.naminhyeok.fantazzk.room.ProvideGameAuctionApi")).isFalse();
        assertThat(isPublic("com.naminhyeok.fantazzk.room.ProvideGameDraftApi")).isFalse();
        assertThat(isPublic("com.naminhyeok.fantazzk.room.CreateRoom")).isFalse();
        assertThat(isPublic("com.naminhyeok.fantazzk.room.GetRoom")).isFalse();
        assertThat(isPublic("com.naminhyeok.fantazzk.room.GetGame")).isFalse();
        assertThat(isPublic("com.naminhyeok.fantazzk.room.Room")).isFalse();
        assertThat(isPublic("com.naminhyeok.fantazzk.room.Game")).isFalse();
        assertThat(isPublic("com.naminhyeok.fantazzk.room.Rooms")).isFalse();
        assertClassMissing("com.naminhyeok.fantazzk.room.JoinableRoomReader");
        assertClassMissing("com.naminhyeok.fantazzk.room.AuctionScheduleReader");
        assertClassMissing("com.naminhyeok.fantazzk.room.AuctionScheduleCandidate");
        assertClassMissing("com.naminhyeok.fantazzk.room.RoomSnapshotPublisher");
        assertClassMissing("com.naminhyeok.fantazzk.room.RoomCodeGenerator");
        assertClassMissing("com.naminhyeok.fantazzk.room.TeamLeaderIdentityIssuer");
        assertClassMissing("com.naminhyeok.fantazzk.room.StartedRoomSnapshot");
        assertClassMissing("com.naminhyeok.fantazzk.room.JoinableRoomJpaRepository");
        assertClassMissing("com.naminhyeok.fantazzk.room.JpaJoinableRoomReader");
        assertClassMissing("com.naminhyeok.fantazzk.room.AuctionScheduleJpaRepository");
        assertClassMissing("com.naminhyeok.fantazzk.room.JpaAuctionScheduleReader");
        assertClassMissing("com.naminhyeok.fantazzk.room.RoomAuctionDeadlineScheduler");
        assertClassMissing("com.naminhyeok.fantazzk.room.RoomAuctionSchedulingPolicy");
        assertClassMissing("com.naminhyeok.fantazzk.room.SupabaseRoomRealtimePublisher");
        assertClassMissing("com.naminhyeok.fantazzk.room.NoopRoomSnapshotPublisher");
        assertClassMissing("com.naminhyeok.fantazzk.room.RealtimeSnapshotEvent");
        assertClassMissing("com.naminhyeok.fantazzk.room.RoomSnapshotUpdatedEvent");
        assertClassMissing("com.naminhyeok.fantazzk.room.GameSnapshotUpdatedEvent");
        assertClassMissing("com.naminhyeok.fantazzk.room.UuidRoomCodeGenerator");
        assertClassMissing("com.naminhyeok.fantazzk.room.UuidTeamLeaderIdentityIssuer");
        assertThat(isPublic("com.naminhyeok.fantazzk.room.application.query.JoinableRoomReader")).isTrue();
        assertThat(isPublic("com.naminhyeok.fantazzk.room.application.query.JoinableRoomSummary")).isTrue();
        assertThat(isPublic("com.naminhyeok.fantazzk.room.application.query.AuctionScheduleReader")).isTrue();
        assertThat(isPublic("com.naminhyeok.fantazzk.room.application.query.AuctionScheduleCandidate")).isTrue();
        assertThat(isPublic("com.naminhyeok.fantazzk.room.application.port.AuctionDeadlineSettlementProcessor")).isTrue();
        assertThat(isPublic("com.naminhyeok.fantazzk.room.application.port.RoomSnapshotPublisher")).isTrue();
        assertThat(isPublic("com.naminhyeok.fantazzk.room.application.port.RoomCodeGenerator")).isTrue();
        assertThat(isPublic("com.naminhyeok.fantazzk.room.application.port.TeamLeaderIdentityIssuer")).isTrue();
        assertThat(isPublic("com.naminhyeok.fantazzk.room.application.support.RoomSnapshot")).isTrue();
        assertThat(isPublic("com.naminhyeok.fantazzk.room.application.support.StartedRoomSnapshot")).isTrue();
        assertThat(isPublic("com.naminhyeok.fantazzk.room.infrastructure.realtime.RealtimeSnapshotEvent")).isTrue();
        assertThat(isPublic("com.naminhyeok.fantazzk.room.infrastructure.realtime.RoomSnapshotUpdatedEvent")).isTrue();
        assertThat(isPublic("com.naminhyeok.fantazzk.room.infrastructure.realtime.GameSnapshotUpdatedEvent")).isTrue();
        assertThat(isPublic("com.naminhyeok.fantazzk.room.infrastructure.realtime.NoopRoomSnapshotPublisher")).isTrue();
        assertThat(isPublic("com.naminhyeok.fantazzk.room.infrastructure.realtime.SupabaseRoomRealtimePublisher")).isTrue();
        assertThat(isPublic("com.naminhyeok.fantazzk.room.infrastructure.scheduling.RoomAuctionDeadlineScheduler")).isTrue();
        assertThat(isPublic("com.naminhyeok.fantazzk.room.infrastructure.scheduling.RoomAuctionSchedulingPolicy")).isTrue();
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
        assertClassMissing("com.naminhyeok.fantazzk.room.RoomSchedulingEvent");
        assertClassMissing("com.naminhyeok.fantazzk.room.RoomStarted");
        assertClassMissing("com.naminhyeok.fantazzk.room.BidPlaced");
        assertClassMissing("com.naminhyeok.fantazzk.room.AuctionSettled");
        assertClassMissing("com.naminhyeok.fantazzk.room.application.support.RoomSchedulingEvent");
        assertClassMissing("com.naminhyeok.fantazzk.room.application.support.RoomStarted");
        assertClassMissing("com.naminhyeok.fantazzk.room.application.support.BidPlaced");
        assertClassMissing("com.naminhyeok.fantazzk.room.application.support.AuctionSettled");
        assertThat(isPublic("com.naminhyeok.fantazzk.room.domain.event.RoomSchedulingEvent")).isTrue();
        assertThat(isPublic("com.naminhyeok.fantazzk.room.domain.event.RoomStarted")).isTrue();
        assertThat(isPublic("com.naminhyeok.fantazzk.room.domain.event.BidPlaced")).isTrue();
        assertThat(isPublic("com.naminhyeok.fantazzk.room.domain.event.AuctionSettled")).isTrue();
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
