package com.naminhyeok.fantazzk.room;

import com.naminhyeok.fantazzk.room.domain.game.*;
import com.naminhyeok.fantazzk.room.domain.handoff.*;
import com.naminhyeok.fantazzk.room.domain.repository.*;
import com.naminhyeok.fantazzk.room.domain.room.*;
import com.naminhyeok.fantazzk.room.domain.shared.*;

import static org.assertj.core.api.Assertions.assertThat;

import com.naminhyeok.fantazzk.room.application.query.FindJoinableRooms;
import java.time.Instant;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestConstructor;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest(
    properties = {
        "spring.datasource.url=jdbc:h2:mem:find-joinable-rooms-test;DB_CLOSE_DELAY=-1",
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
class FindJoinableRoomsIntegrationTest {
    private final FindJoinableRooms findJoinableRooms;
    private final Rooms rooms;

    @Test
    void 참여_가능한_waiting_room만_최신순과_안정적인_tie_break로_반환한다() {
        Instant createdAt = Instant.parse("2026-04-09T00:00:00Z");
        rooms.save(fullWaitingRoom("ROOM10", createdAt));
        rooms.save(waitingRoom("ROOM09", createdAt));
        rooms.save(fullWaitingRoom("ROOM08", createdAt));
        rooms.save(waitingRoom("ROOM07", createdAt));
        rooms.save(fullWaitingRoom("ROOM06", createdAt));
        rooms.save(waitingRoom("ROOM05", createdAt));
        rooms.save(fullWaitingRoom("ROOM04", createdAt));
        rooms.save(waitingRoom("ROOM03", createdAt));
        rooms.save(fullWaitingRoom("ROOM02", createdAt));
        rooms.save(waitingRoom("ROOM01", createdAt));

        assertThat(findJoinableRooms.list())
            .hasSize(5)
            .extracting("code")
            .containsExactly("ROOM09", "ROOM07", "ROOM05", "ROOM03", "ROOM01");
    }

    @Test
    void 참여_가능한_room은_createdAt_내림차순을_우선하고_code는_동률_보조정렬로_쓴다() {
        rooms.save(waitingRoom("ROOM99", Instant.parse("2026-04-09T00:00:00Z")));
        rooms.save(waitingRoom("ROOM01", Instant.parse("2026-04-09T00:02:00Z")));
        rooms.save(waitingRoom("ROOM50", Instant.parse("2026-04-09T00:01:00Z")));

        assertThat(findJoinableRooms.list())
            .extracting("code")
            .containsExactly("ROOM01", "ROOM50", "ROOM99");
    }

    private Room waitingRoom(String code, Instant createdAt) {
        return Room.createFromTemplate(
            code,
            new TeamLeaderId("host-" + code),
            "호스트-" + code,
            "token-" + code,
            new RoomTemplateSpec(
                RoomMode.AUCTION,
                2,
                2,
                300,
                15,
                10,
                null,
                List.of(
                    new RoomTemplateSpec.Player(new RoomPlayerId(0), "선수1", "TOP", 0),
                    new RoomTemplateSpec.Player(new RoomPlayerId(1), "선수2", "JUNGLE", 1)
                )
            ),
            createdAt
        );
    }

    private Room fullWaitingRoom(String code, Instant createdAt) {
        Room room = waitingRoom(code, createdAt);
        room.join(new TeamLeaderId("guest-" + code), "게스트-" + code, "guest-action-token-" + code);
        return room;
    }
}
