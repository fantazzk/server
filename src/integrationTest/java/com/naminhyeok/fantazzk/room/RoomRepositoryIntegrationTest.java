package com.naminhyeok.fantazzk.room;

import static org.assertj.core.api.Assertions.assertThat;

import com.naminhyeok.fantazzk.room.domain.Room;
import com.naminhyeok.fantazzk.room.domain.RoomPlayer;
import com.naminhyeok.fantazzk.room.domain.RoomTeamLeader;
import com.naminhyeok.fantazzk.room.domain.RoomTemplateSpec;
import com.naminhyeok.fantazzk.room.repository.Rooms;
import java.util.List;
import java.util.UUID;
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
    private final Rooms rooms;

    @Test
    @Transactional
    void 방과_내부_선수_팀장_컬렉션을_저장하고_다시_읽는다() {
        Room saved =
            rooms.save(
                Room.createFromTemplate(
                    "ROOM01",
                    UUID.randomUUID().toString(),
                    "호스트",
                    new RoomTemplateSpec(
                        RoomTemplateSpec.Mode.AUCTION,
                        2,
                        2,
                        300,
                        null,
                        List.of(
                            new RoomTemplateSpec.Player("선수1", 0),
                            new RoomTemplateSpec.Player("선수2", 1)
                        )
                    )
                )
            );

        Room reloaded = rooms.findById(saved.getId()).orElseThrow();

        assertThat(reloaded.getId()).isEqualTo(saved.getId());
        assertThat(reloaded.getCode()).isEqualTo("ROOM01");
        assertThat(reloaded.getPlayers().stream().map(RoomPlayer::getName)).containsExactly("선수1", "선수2");
        assertThat(reloaded.getLeaders()).singleElement().extracting(RoomTeamLeader::getNickname).isEqualTo("호스트");
    }
}
