package com.naminhyeok.fantazzk.room;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.naminhyeok.fantazzk.CoreException;
import com.naminhyeok.fantazzk.template.TemplateFixture;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestConstructor;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest(
    properties = {
        "spring.datasource.url=jdbc:h2:mem:room-service-test;DB_CLOSE_DELAY=-1",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.datasource.username=sa",
        "spring.datasource.password=",
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "spring.liquibase.enabled=false",
        "sentry.enabled=false"
    }
)
@Transactional
@TestConstructor(autowireMode = TestConstructor.AutowireMode.ALL)
@RequiredArgsConstructor
class RoomServiceIntegrationTest {
    private final TemplateFixture templateFixture;
    private final CreateRoom createRoom;
    private final JoinRoom joinRoom;
    private final StartRoom startRoom;
    private final SelectDraftPosition selectDraftPosition;
    private final ClearDraftPosition clearDraftPosition;
    private final Rooms rooms;

    @Test
    void 템플릿으로_방을_생성하면_호스트_액션_토큰을_발급하고_저장한다() {
        var template =
            templateFixture.createAuctionTemplateId("경매전", 2, 2, 300, List.of("선수1", "선수2"));

        Room created = createRoom.create(template, "호스트");
        Room reloaded = rooms.findById(created.getId()).orElseThrow();

        assertThat(reloaded.getStatus()).isEqualTo(RoomStatus.WAITING);
        assertThat(reloaded.getLeaders()).singleElement()
            .extracting(RoomTeamLeader::getNickname, RoomTeamLeader::getActionToken)
            .satisfies(tuple -> {
                assertThat(tuple.get(0)).isEqualTo("호스트");
                assertThat(tuple.get(1)).asString().isNotBlank();
            });
    }

    @Test
    void 호스트가_아닌_액션_토큰으로는_방을_시작할_수_없다() {
        var template =
            templateFixture.createAuctionTemplateId("경매전", 2, 2, 300, List.of("선수1", "선수2"));

        Room created = createRoom.create(template, "호스트");
        RoomTeamLeader guest = joinRoom.join(created.getCode(), "게스트");

        assertThatThrownBy(() -> startRoom.start(created.getCode(), guest.getActionToken()))
            .isInstanceOf(CoreException.class)
            .satisfies(ex -> {
                CoreException coreException = (CoreException) ex;
                assertThat(coreException.getError()).isEqualTo(RoomErrorType.ROOM_START_FORBIDDEN);
                assertThat(coreException.getData()).isNull();
            });
    }

    @Test
    void 드래프트_자리가_모두_확정되면_시작_가능_상태가_된다() {
        var template =
            templateFixture.createDraftTemplateId("드래프트전", 2, 2, com.naminhyeok.fantazzk.template.TemplateCatalog.DraftOrderStrategy.SNAKE, List.of("선수1", "선수2"));

        Room created = createRoom.create(template, "호스트");
        RoomTeamLeader guest = joinRoom.join(created.getCode(), "게스트");

        selectDraftPosition.select(created.getCode(), created.getLeaders().getFirst().getActionToken(), 2);
        selectDraftPosition.select(created.getCode(), guest.getActionToken(), 1);

        Room reloaded = rooms.findByCode(created.getCode()).orElseThrow();

        assertThat(reloaded.getStartReadiness()).isEqualTo(RoomStartReadiness.STARTABLE);
        assertThat(reloaded.getLeaders())
            .extracting(RoomTeamLeader::getTeamLeaderId, RoomTeamLeader::getDraftPosition)
            .containsExactlyInAnyOrder(
                org.assertj.core.groups.Tuple.tuple(created.getLeaders().getFirst().getTeamLeaderId(), 2),
                org.assertj.core.groups.Tuple.tuple(guest.getTeamLeaderId(), 1)
            );
    }

    @Test
    void 드래프트_자리가_미확정이면_방을_시작할_수_없다() {
        var template =
            templateFixture.createDraftTemplateId("드래프트전", 2, 2, com.naminhyeok.fantazzk.template.TemplateCatalog.DraftOrderStrategy.SNAKE, List.of("선수1", "선수2"));

        Room created = createRoom.create(template, "호스트");
        joinRoom.join(created.getCode(), "게스트");
        selectDraftPosition.select(created.getCode(), created.getLeaders().getFirst().getActionToken(), 1);

        assertThatThrownBy(() -> startRoom.start(created.getCode(), created.getLeaders().getFirst().getActionToken()))
            .isInstanceOf(CoreException.class)
            .satisfies(ex -> {
                CoreException coreException = (CoreException) ex;
                assertThat(coreException.getError()).isEqualTo(RoomErrorType.ROOM_DRAFT_POSITIONS_NOT_FULL);
                assertThat(coreException.getData()).isNull();
            });
    }

    @Test
    void 드래프트_자리를_취소하면_다시_미선택이_된다() {
        var template =
            templateFixture.createDraftTemplateId("드래프트전", 2, 2, com.naminhyeok.fantazzk.template.TemplateCatalog.DraftOrderStrategy.SNAKE, List.of("선수1", "선수2"));

        Room created = createRoom.create(template, "호스트");

        selectDraftPosition.select(created.getCode(), created.getLeaders().getFirst().getActionToken(), 1);
        clearDraftPosition.clear(created.getCode(), created.getLeaders().getFirst().getActionToken());

        Room reloaded = rooms.findByCode(created.getCode()).orElseThrow();

        assertThat(reloaded.getLeaders()).singleElement()
            .extracting(RoomTeamLeader::getDraftPosition)
            .isNull();
    }
}
