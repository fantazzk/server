package com.naminhyeok.fantazzk.room;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.groups.Tuple.tuple;

import com.naminhyeok.fantazzk.CoreException;
import com.naminhyeok.fantazzk.template.TemplateCatalog.DraftOrderStrategy;
import com.naminhyeok.fantazzk.template.TemplateFixture;
import jakarta.persistence.EntityManager;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestConstructor;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest(
    properties = {
        "spring.datasource.url=jdbc:h2:mem:room-draft-test;DB_CLOSE_DELAY=-1",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.datasource.username=sa",
        "spring.datasource.password=",
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "spring.liquibase.enabled=false",
        "sentry.enabled=false"
    }
)
@TestConstructor(autowireMode = TestConstructor.AutowireMode.ALL)
@RequiredArgsConstructor
class RoomDraftIntegrationTest {
    private final TemplateFixture templateFixture;
    private final CreateRoom createRoom;
    private final JoinRoom joinRoom;
    private final StartRoom startRoom;
    private final SelectDraftPosition selectDraftPosition;
    private final PickDraft pickDraft;
    private final Rooms rooms;
    private final EntityManager entityManager;

    @Test
    @Transactional
    void 픽을_처리하면_선수_배정과_턴_진행이_반영된다() {
        var template =
            templateFixture.createDraftTemplateId(
                "드래프트전",
                2,
                2,
                DraftOrderStrategy.SNAKE,
                List.of("선수1", "선수2")
            );

        Room created = createRoom.create(template, "호스트");
        RoomTeamLeader guest = joinRoom.join(created.getCode(), "게스트");
        selectDraftPosition.select(created.getCode(), created.getLeaders().getFirst().getActionToken(), 2);
        selectDraftPosition.select(created.getCode(), guest.getActionToken(), 1);
        startRoom.start(created.getCode(), created.getLeaders().getFirst().getActionToken());

        String currentLeaderId = guest.getId().value();
        RoomTeamMember member = pickDraft.pick(created.getCode(), currentLeaderId, "선수1");

        Room reloaded = rooms.findByCode(created.getCode()).orElseThrow();

        assertThat(member.getPlayerName()).isEqualTo("선수1");
        assertThat(reloaded.getMembers()).singleElement()
            .extracting(RoomTeamMember::teamLeaderId, RoomTeamMember::getPlayerId, RoomTeamMember::getPlayerName)
            .containsExactly(new TeamLeaderId(currentLeaderId), new RoomPlayerId(0), "선수1");
        assertThat(reloaded.getPlayers().stream().filter(it -> it.getName().equals("선수1")).findFirst().orElseThrow().getStatus())
            .isEqualTo(PlayerStatus.ASSIGNED);
        assertThat(reloaded.getCurrentTurnIndex()).isEqualTo(1);
    }

    @Test
    void 픽_요청은_선수이름_해석보다_턴_검증을_먼저_적용한다() {
        var template =
            templateFixture.createDraftTemplateId(
                "드래프트전",
                2,
                2,
                DraftOrderStrategy.SNAKE,
                List.of("선수1", "선수2")
            );

        Room created = createRoom.create(template, "호스트");
        RoomTeamLeader guest = joinRoom.join(created.getCode(), "게스트");
        selectDraftPosition.select(created.getCode(), created.getLeaders().getFirst().getActionToken(), 2);
        selectDraftPosition.select(created.getCode(), guest.getActionToken(), 1);
        startRoom.start(created.getCode(), created.getLeaders().getFirst().getActionToken());

        assertThatThrownBy(() -> pickDraft.pick(created.getCode(), created.getLeaders().getFirst().getTeamLeaderId(), "없는선수"))
            .isInstanceOf(CoreException.class)
            .isInstanceOfSatisfying(CoreException.class, ex -> assertThat(ex.getError()).isEqualTo(RoomErrorType.ROOM_PICK_OUT_OF_TURN));
    }

    @Test
    @Transactional
    void SNAKE_드래프트는_재조회_후에도_2라운드가_역순으로_진행된다() {
        var template =
            templateFixture.createDraftTemplateId(
                "드래프트전",
                2,
                3,
                DraftOrderStrategy.SNAKE,
                List.of("선수1", "선수2", "선수3", "선수4")
            );

        Room created = createRoom.create(template, "호스트");
        RoomTeamLeader guest = joinRoom.join(created.getCode(), "게스트");
        RoomTeamLeader host = created.getLeaders().getFirst();
        selectDraftPosition.select(created.getCode(), host.getActionToken(), 1);
        selectDraftPosition.select(created.getCode(), guest.getActionToken(), 2);
        startRoom.start(created.getCode(), host.getActionToken());

        pickDraft.pick(created.getCode(), host.getTeamLeaderId(), "선수1");
        pickDraft.pick(created.getCode(), guest.getTeamLeaderId(), "선수2");

        entityManager.flush();
        entityManager.clear();
        Room reloadedAfterSecondPick = rooms.findByCode(created.getCode()).orElseThrow();

        assertThat(reloadedAfterSecondPick.getCurrentTurnIndex()).isEqualTo(2);

        RoomTeamMember thirdPick = pickDraft.pick(created.getCode(), guest.getTeamLeaderId(), "선수3");

        entityManager.flush();
        entityManager.clear();
        Room reloadedAfterThirdPick = rooms.findByCode(created.getCode()).orElseThrow();

        assertThat(thirdPick.getTeamLeaderId()).isEqualTo(guest.getTeamLeaderId());
        assertThat(reloadedAfterThirdPick.getMembers())
            .extracting(RoomTeamMember::getTeamLeaderId, RoomTeamMember::getPlayerName)
            .containsExactly(
                tuple(host.getTeamLeaderId(), "선수1"),
                tuple(guest.getTeamLeaderId(), "선수2"),
                tuple(guest.getTeamLeaderId(), "선수3")
            );
        assertThat(reloadedAfterThirdPick.getCurrentTurnIndex()).isEqualTo(3);
    }
}
