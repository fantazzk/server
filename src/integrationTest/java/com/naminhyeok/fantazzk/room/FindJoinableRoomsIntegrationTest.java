package com.naminhyeok.fantazzk.room;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;
import java.util.List;
import org.springframework.data.domain.PageRequest;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
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
@Import(FindJoinableRooms.class)
@TestConstructor(autowireMode = TestConstructor.AutowireMode.ALL)
@RequiredArgsConstructor
class FindJoinableRoomsIntegrationTest {
    private final FindJoinableRooms findJoinableRooms;
    private final Rooms rooms;

    @Test
    void 참여_가능한_waiting_room만_최신순으로_반환한다() {
        rooms.save(waitingRoom("ROOM01", Instant.parse("2026-04-09T00:00:00Z")));

        Room full = waitingRoom("ROOM02", Instant.parse("2026-04-09T00:01:00Z"));
        full.join("guest-1", "게스트", "guest-action-token");
        rooms.save(full);

        Room started = waitingRoom("ROOM03", Instant.parse("2026-04-09T00:02:00Z"));
        started.join("guest-2", "게스트", "guest-action-token-2");
        started.start("host-ROOM03");
        rooms.save(started);

        rooms.save(waitingRoom("ROOM04", Instant.parse("2026-04-09T00:03:00Z")));

        assertThat(findJoinableRooms.list()).extracting(Room::getCode)
            .containsExactly("ROOM04", "ROOM01");
    }

    @Test
    void 참여_가능한_room은_최대_다섯개까지만_반환한다() {
        List.of("ROOM01", "ROOM02", "ROOM03", "ROOM04", "ROOM05", "ROOM06")
            .forEach(
                code ->
                    rooms.save(
                        waitingRoom(code, Instant.parse("2026-04-09T00:00:00Z").plusSeconds(code.charAt(5)))
                    )
            );

        assertThat(rooms.findByStatusOrderByCreatedAtDesc(RoomStatus.WAITING, PageRequest.of(0, 5)).getContent())
            .extracting(Room::getCode)
            .containsExactly("ROOM06", "ROOM05", "ROOM04", "ROOM03", "ROOM02");

        assertThat(findJoinableRooms.list())
            .hasSize(5)
            .extracting(Room::getCode)
            .containsExactly("ROOM06", "ROOM05", "ROOM04", "ROOM03", "ROOM02");
    }

    private Room waitingRoom(String code, Instant createdAt) {
        return Room.createFromTemplate(
            code,
            "host-" + code,
            "호스트-" + code,
            "token-" + code,
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
            ),
            createdAt
        );
    }
}
