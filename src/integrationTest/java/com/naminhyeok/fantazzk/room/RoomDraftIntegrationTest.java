package com.naminhyeok.fantazzk.room;

import com.naminhyeok.fantazzk.room.domain.game.*;
import com.naminhyeok.fantazzk.room.domain.handoff.*;
import com.naminhyeok.fantazzk.room.domain.repository.*;
import com.naminhyeok.fantazzk.room.domain.room.*;
import com.naminhyeok.fantazzk.room.domain.shared.*;

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
    private final Games games;
    private final EntityManager entityManager;

    @Test
    @Transactional
    void 픽을_처리하면_DraftGame에만_선수_배정과_턴_진행이_반영된다() {
        var template =
            templateFixture.createDraftTemplateId(
                "드래프트전",
                2,
                2,
                DraftOrderStrategy.SNAKE,
                List.of(
                    new TemplateFixture.PlayerSpec("선수1", "TOP"),
                    new TemplateFixture.PlayerSpec("선수2", "JUNGLE")
                )
            );

        RoomSessionResult created = createRoom.create(template, "호스트");
        RoomTeamLeader guest = joinRoom.join(created.room().getCode(), "게스트").leader();
        selectDraftPosition.select(created.room().getCode(), created.leader().getActionToken(), 2);
        selectDraftPosition.select(created.room().getCode(), guest.getActionToken(), 1);
        startRoom.start(created.room().getCode(), created.leader().getActionToken());

        RosterMember member = pickDraft.pick(created.room().getCode(), guest.getActionToken(), "선수1");

        entityManager.flush();
        entityManager.clear();
        Room reloaded = rooms.findByCode(created.room().getCode()).orElseThrow();
        DraftGame game = (DraftGame) games.findById(reloaded.getStartedGameId()).orElseThrow();

        assertThat(member.playerName()).isEqualTo("선수1");
        assertThat(reloaded.getStatus()).isEqualTo(RoomStatus.STARTED);
        assertThat(reloaded.getPlayers().stream().filter(it -> it.getName().equals("선수1")).findFirst().orElseThrow().getStatus())
            .isEqualTo(PlayerStatus.AVAILABLE);
        assertThat(game.getMembers()).singleElement()
            .extracting(RosterMember::teamLeaderId, RosterMember::playerName)
            .containsExactly(guest.getId(), "선수1");
        assertThat(game.isPlayerAvailable("선수1")).isFalse();
        assertThat(game.getCurrentTurnIndex()).isEqualTo(1);
    }

    @Test
    void 픽_요청은_선수이름_해석보다_턴_검증을_먼저_적용한다() {
        var template =
            templateFixture.createDraftTemplateId(
                "드래프트전",
                2,
                2,
                DraftOrderStrategy.SNAKE,
                List.of(
                    new TemplateFixture.PlayerSpec("선수1", "TOP"),
                    new TemplateFixture.PlayerSpec("선수2", "JUNGLE")
                )
            );

        RoomSessionResult created = createRoom.create(template, "호스트");
        RoomTeamLeader guest = joinRoom.join(created.room().getCode(), "게스트").leader();
        selectDraftPosition.select(created.room().getCode(), created.leader().getActionToken(), 2);
        selectDraftPosition.select(created.room().getCode(), guest.getActionToken(), 1);
        startRoom.start(created.room().getCode(), created.leader().getActionToken());

        assertThatThrownBy(() -> pickDraft.pick(created.room().getCode(), created.leader().getActionToken(), "없는선수"))
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
                List.of(
                    new TemplateFixture.PlayerSpec("선수1", "TOP"),
                    new TemplateFixture.PlayerSpec("선수2", "JUNGLE"),
                    new TemplateFixture.PlayerSpec("선수3", "MID"),
                    new TemplateFixture.PlayerSpec("선수4", "ADC")
                )
            );

        RoomSessionResult created = createRoom.create(template, "호스트");
        RoomTeamLeader guest = joinRoom.join(created.room().getCode(), "게스트").leader();
        RoomTeamLeader host = created.leader();
        selectDraftPosition.select(created.room().getCode(), host.getActionToken(), 1);
        selectDraftPosition.select(created.room().getCode(), guest.getActionToken(), 2);
        startRoom.start(created.room().getCode(), host.getActionToken());

        pickDraft.pick(created.room().getCode(), host.getActionToken(), "선수1");
        pickDraft.pick(created.room().getCode(), guest.getActionToken(), "선수2");

        entityManager.flush();
        entityManager.clear();
        Room reloadedAfterSecondPick = rooms.findByCode(created.room().getCode()).orElseThrow();
        DraftGame gameAfterSecondPick = (DraftGame) games.findById(reloadedAfterSecondPick.getStartedGameId()).orElseThrow();

        assertThat(reloadedAfterSecondPick.getStatus()).isEqualTo(RoomStatus.STARTED);
        assertThat(gameAfterSecondPick.getCurrentTurnIndex()).isEqualTo(2);

        RosterMember thirdPick = pickDraft.pick(created.room().getCode(), guest.getActionToken(), "선수3");

        entityManager.flush();
        entityManager.clear();
        Room reloadedAfterThirdPick = rooms.findByCode(created.room().getCode()).orElseThrow();
        DraftGame gameAfterThirdPick = (DraftGame) games.findById(reloadedAfterThirdPick.getStartedGameId()).orElseThrow();

        assertThat(thirdPick.teamLeaderId()).isEqualTo(guest.getId());
        assertThat(gameAfterThirdPick.getMembers())
            .extracting(RosterMember::teamLeaderId, RosterMember::playerName)
            .containsExactly(
                tuple(host.getId(), "선수1"),
                tuple(guest.getId(), "선수2"),
                tuple(guest.getId(), "선수3")
            );
        assertThat(reloadedAfterThirdPick.getStatus()).isEqualTo(RoomStatus.STARTED);
        assertThat(gameAfterThirdPick.getCurrentTurnIndex()).isEqualTo(3);
    }
}
