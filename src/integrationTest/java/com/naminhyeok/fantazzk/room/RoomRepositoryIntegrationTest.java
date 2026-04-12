package com.naminhyeok.fantazzk.room;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import jakarta.persistence.EntityManager;
import java.time.Instant;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestConstructor;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest(
    properties = {
        "spring.datasource.url=jdbc:h2:mem:room-repository-test;DB_CLOSE_DELAY=-1",
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
class RoomRepositoryIntegrationTest {
    private static final Instant CREATED_AT = Instant.parse("2026-04-09T00:00:00Z");

    private final Rooms rooms;
    private final EntityManager entityManager;

    @Test
    @Transactional
    void 방과_내부_선수_팀장_컬렉션을_저장하고_다시_읽는다() {
        Room room =
            Room.createFromTemplate(
                "ROOM01",
                new TeamLeaderId("host-1"),
                "호스트",
                "host-action-token",
                new RoomTemplateSpec(
                    RoomTemplateSpec.Mode.DRAFT,
                    2,
                    2,
                    null,
                    30,
                    null,
                    RoomTemplateSpec.DraftOrderStrategy.SNAKE,
                    List.of(
                        new RoomTemplateSpec.Player(new RoomPlayerId(0), "선수1", "TOP", 0),
                        new RoomTemplateSpec.Player(new RoomPlayerId(1), "선수2", "JUNGLE", 1)
                    )
                ),
                CREATED_AT
            );
        room.join(new TeamLeaderId("guest-1"), "게스트", "guest-action-token");
        room.selectDraftPosition(new TeamLeaderId("host-1"), 2);
        room.selectDraftPosition(new TeamLeaderId("guest-1"), 1);

        Room saved = rooms.save(room);
        entityManager.flush();
        entityManager.clear();

        Room reloaded = rooms.findById(saved.getId()).orElseThrow();

        assertThat(reloaded).isNotSameAs(saved);
        assertThat(reloaded.getId()).isEqualTo(saved.getId());
        assertThat(reloaded.getCode()).isEqualTo("ROOM01");
        assertThat(reloaded.getCreatedAt()).isEqualTo(CREATED_AT);
        assertThat(reloaded.getPickBanTime()).isEqualTo(30);
        assertThat(reloaded.getPlayers()).extracting(RoomPlayer::getId)
            .containsExactly(new RoomPlayerId(0), new RoomPlayerId(1));
        assertThat(reloaded.getPlayers().stream().map(RoomPlayer::getName)).containsExactly("선수1", "선수2");
        assertThat(reloaded.getPlayers().stream().map(RoomPlayer::getPosition)).containsExactly("TOP", "JUNGLE");
        assertThat(reloaded.getLeaders())
            .extracting(RoomTeamLeader::getId, RoomTeamLeader::getNickname, RoomTeamLeader::getActionToken, RoomTeamLeader::getDraftPosition)
            .containsExactlyInAnyOrder(
                org.assertj.core.groups.Tuple.tuple(new TeamLeaderId("host-1"), "호스트", "host-action-token", 2),
                org.assertj.core.groups.Tuple.tuple(new TeamLeaderId("guest-1"), "게스트", "guest-action-token", 1)
            );
    }

    @Test
    @Transactional
    void 방의_createdAt을_저장하고_다시_읽는다() {
        Room room =
            Room.createFromTemplate(
                "ROOM02",
                new TeamLeaderId("host-1"),
                "호스트",
                "host-action-token",
                new RoomTemplateSpec(
                    RoomTemplateSpec.Mode.DRAFT,
                    2,
                    2,
                    null,
                    30,
                    null,
                    RoomTemplateSpec.DraftOrderStrategy.SNAKE,
                    List.of(
                        new RoomTemplateSpec.Player(new RoomPlayerId(0), "선수1", "TOP", 0),
                        new RoomTemplateSpec.Player(new RoomPlayerId(1), "선수2", "JUNGLE", 1)
                    )
                ),
                CREATED_AT
            );

        Room saved = rooms.save(room);
        entityManager.flush();
        entityManager.clear();

        Room reloaded = rooms.findById(saved.getId()).orElseThrow();

        assertThat(reloaded.getCreatedAt()).isEqualTo(CREATED_AT);
    }

    @Test
    void 방의_createdAt은_null일_수_없다() {
        assertThatThrownBy(() ->
            Room.createFromTemplate(
                "ROOM03",
                new TeamLeaderId("host-1"),
                "호스트",
                "host-action-token",
                new RoomTemplateSpec(
                    RoomTemplateSpec.Mode.DRAFT,
                    2,
                    2,
                    null,
                    30,
                    null,
                    RoomTemplateSpec.DraftOrderStrategy.SNAKE,
                    List.of(
                        new RoomTemplateSpec.Player(new RoomPlayerId(0), "선수1", "TOP", 0),
                        new RoomTemplateSpec.Player(new RoomPlayerId(1), "선수2", "JUNGLE", 1)
                    )
                ),
                null
            )
        ).isInstanceOf(NullPointerException.class);
    }
}
