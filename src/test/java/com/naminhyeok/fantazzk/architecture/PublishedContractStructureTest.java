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
        assertPublicClasses(
            "com.naminhyeok.fantazzk.room.RoomQueryApi",
            "com.naminhyeok.fantazzk.room.RoomSessionApi",
            "com.naminhyeok.fantazzk.room.RoomDraftApi",
            "com.naminhyeok.fantazzk.room.RoomStartApi",
            "com.naminhyeok.fantazzk.room.GameQueryApi",
            "com.naminhyeok.fantazzk.room.GameAuctionApi",
            "com.naminhyeok.fantazzk.room.GameDraftApi",
            "com.naminhyeok.fantazzk.room.JoinableRoomView",
            "com.naminhyeok.fantazzk.room.RoomView",
            "com.naminhyeok.fantazzk.room.RoomSessionView",
            "com.naminhyeok.fantazzk.room.GameView"
        );
        assertPackagePrivateClasses(
            "com.naminhyeok.fantazzk.room.ProvideRoomQueryApi",
            "com.naminhyeok.fantazzk.room.ProvideRoomSessionApi",
            "com.naminhyeok.fantazzk.room.ProvideRoomDraftApi",
            "com.naminhyeok.fantazzk.room.ProvideRoomStartApi",
            "com.naminhyeok.fantazzk.room.ProvideGameQueryApi",
            "com.naminhyeok.fantazzk.room.ProvideGameAuctionApi",
            "com.naminhyeok.fantazzk.room.ProvideGameDraftApi"
        );
        assertClassesMissing(
            "com.naminhyeok.fantazzk.room.RoomManagement",
            "com.naminhyeok.fantazzk.room.JoinableRoomResponse",
            "com.naminhyeok.fantazzk.room.RoomViewResponse",
            "com.naminhyeok.fantazzk.room.RoomSessionResponse",
            "com.naminhyeok.fantazzk.room.GameResponse",
            "com.naminhyeok.fantazzk.room.RoomApiController",
            "com.naminhyeok.fantazzk.room.CreateRoom",
            "com.naminhyeok.fantazzk.room.JoinRoom",
            "com.naminhyeok.fantazzk.room.GetRoom",
            "com.naminhyeok.fantazzk.room.StartRoom",
            "com.naminhyeok.fantazzk.room.SelectDraftPosition",
            "com.naminhyeok.fantazzk.room.ClearDraftPosition",
            "com.naminhyeok.fantazzk.room.GetGame",
            "com.naminhyeok.fantazzk.room.PlaceBid",
            "com.naminhyeok.fantazzk.room.PickDraft",
            "com.naminhyeok.fantazzk.room.SettleAuction",
            "com.naminhyeok.fantazzk.room.FindJoinableRooms",
            "com.naminhyeok.fantazzk.room.CreateRoomAttempt",
            "com.naminhyeok.fantazzk.room.SettleAuctionAttempt",
            "com.naminhyeok.fantazzk.room.StartedGameContextLoader",
            "com.naminhyeok.fantazzk.room.RoomActionAuthorizer",
            "com.naminhyeok.fantazzk.room.RoomSessionResult",
            "com.naminhyeok.fantazzk.room.JoinableRoomReader",
            "com.naminhyeok.fantazzk.room.AuctionScheduleReader",
            "com.naminhyeok.fantazzk.room.AuctionScheduleCandidate",
            "com.naminhyeok.fantazzk.room.RoomSnapshotPublisher",
            "com.naminhyeok.fantazzk.room.RoomCodeGenerator",
            "com.naminhyeok.fantazzk.room.TeamLeaderIdentityIssuer",
            "com.naminhyeok.fantazzk.room.StartedRoomSnapshot",
            "com.naminhyeok.fantazzk.room.JoinableRoomJpaRepository",
            "com.naminhyeok.fantazzk.room.JpaJoinableRoomReader",
            "com.naminhyeok.fantazzk.room.AuctionScheduleJpaRepository",
            "com.naminhyeok.fantazzk.room.JpaAuctionScheduleReader",
            "com.naminhyeok.fantazzk.room.RoomAuctionDeadlineScheduler",
            "com.naminhyeok.fantazzk.room.RoomAuctionSchedulingPolicy",
            "com.naminhyeok.fantazzk.room.SupabaseRoomRealtimePublisher",
            "com.naminhyeok.fantazzk.room.NoopRoomSnapshotPublisher",
            "com.naminhyeok.fantazzk.room.RealtimeSnapshotEvent",
            "com.naminhyeok.fantazzk.room.RoomSnapshotUpdatedEvent",
            "com.naminhyeok.fantazzk.room.GameSnapshotUpdatedEvent",
            "com.naminhyeok.fantazzk.room.UuidRoomCodeGenerator",
            "com.naminhyeok.fantazzk.room.UuidTeamLeaderIdentityIssuer",
            "com.naminhyeok.fantazzk.room.Room",
            "com.naminhyeok.fantazzk.room.Game",
            "com.naminhyeok.fantazzk.room.Rooms",
            "com.naminhyeok.fantazzk.room.domain.game.GameId",
            "com.naminhyeok.fantazzk.room.domain.room.RoomId",
            "com.naminhyeok.fantazzk.room.domain.room.RoomMode",
            "com.naminhyeok.fantazzk.room.domain.room.RoomErrorType",
            "com.naminhyeok.fantazzk.room.domain.room.RoomStateInvalidException"
        );
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
        Class<?> teamLeaderId = Class.forName("com.naminhyeok.fantazzk.room.domain.shared.TeamLeaderId");

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

    private void assertPublicClasses(String... classNames) throws Exception {
        for (String className : classNames) {
            assertThat(isPublic(className)).as(className).isTrue();
        }
    }

    private void assertPackagePrivateClasses(String... classNames) throws Exception {
        for (String className : classNames) {
            assertThat(isPublic(className)).as(className).isFalse();
        }
    }

    private void assertClassMissing(String className) {
        assertThatThrownBy(() -> Class.forName(className))
            .isInstanceOf(ClassNotFoundException.class);
    }

    private void assertClassesMissing(String... classNames) {
        for (String className : classNames) {
            assertClassMissing(className);
        }
    }

    private boolean hasDeclaredMethod(String name, Class<?>... parameterTypes) {
        try {
            Method ignored = Class.forName("com.naminhyeok.fantazzk.room.domain.room.Room").getDeclaredMethod(name, parameterTypes);
            return true;
        } catch (NoSuchMethodException ex) {
            return false;
        } catch (ClassNotFoundException ex) {
            throw new AssertionError(ex);
        }
    }
}
