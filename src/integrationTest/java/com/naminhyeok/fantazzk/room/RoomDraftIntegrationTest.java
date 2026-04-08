package com.naminhyeok.fantazzk.room;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.UUID;
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
    private final com.naminhyeok.fantazzk.template.TemplateManagement templateManagement;
    private final CreateRoom createRoom;
    private final JoinRoom joinRoom;
    private final StartRoom startRoom;
    private final PickDraft pickDraft;
    private final Rooms rooms;

    @Test
    @Transactional
    void 픽을_처리하면_선수_배정과_턴_진행이_반영된다() {
        var template =
            templateManagement.create(
                new com.naminhyeok.fantazzk.template.CreateTemplateInput(
                    "드래프트전",
                    com.naminhyeok.fantazzk.template.TemplateCatalog.Mode.DRAFT,
                    2,
                    2,
                    null,
                    com.naminhyeok.fantazzk.template.TemplateCatalog.DraftOrderStrategy.SNAKE,
                    List.of("선수1", "선수2")
                )
            );

        Room created = createRoom.create(UUID.fromString(template.id()), "호스트");
        joinRoom.join(created.getCode(), "게스트");
        startRoom.start(created.getCode());

        String currentLeaderId = rooms.findByCode(created.getCode()).orElseThrow().getLeaders().getFirst().getTeamLeaderId();
        RoomTeamMember member = pickDraft.pick(created.getCode(), currentLeaderId, "선수1");

        Room reloaded = rooms.findByCode(created.getCode()).orElseThrow();

        assertThat(member.getPlayerName()).isEqualTo("선수1");
        assertThat(reloaded.getMembers()).singleElement()
            .extracting(RoomTeamMember::getTeamLeaderId, RoomTeamMember::getPlayerName)
            .containsExactly(currentLeaderId, "선수1");
        assertThat(reloaded.getPlayers().stream().filter(it -> it.getName().equals("선수1")).findFirst().orElseThrow().getStatus())
            .isEqualTo(PlayerStatus.ASSIGNED);
        assertThat(reloaded.getCurrentTurnIndex()).isEqualTo(1);
    }
}
