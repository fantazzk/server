package com.naminhyeok.fantazzk.room;

import static org.assertj.core.api.Assertions.assertThat;

import com.naminhyeok.fantazzk.template.CreateTemplate;
import com.naminhyeok.fantazzk.template.CreateTemplateCommand;
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
@TestConstructor(autowireMode = TestConstructor.AutowireMode.ALL)
@RequiredArgsConstructor
class RoomServiceIntegrationTest {
    private final CreateTemplate createTemplate;
    private final CreateRoom createRoom;
    private final JoinRoom joinRoom;
    private final StartRoom startRoom;
    private final Rooms rooms;

    @Test
    @Transactional
    void 템플릿으로_방을_생성하면_대기_상태와_호스트_팀장을_저장한다() {
        var template =
            createTemplate.create(
                new CreateTemplateCommand.Auction(
                    "경매전",
                    2,
                    2,
                    300,
                    List.of("선수1", "선수2")
                )
            );

        Room created = createRoom.create(template.getId(), "호스트");
        Room reloaded = rooms.findById(created.getId()).orElseThrow();

        assertThat(reloaded.getStatus()).isEqualTo(RoomStatus.WAITING);
        assertThat(reloaded.getLeaders()).singleElement().extracting(RoomTeamLeader::getNickname).isEqualTo("호스트");
    }

    @Test
    @Transactional
    void 참가와_시작을_순서대로_처리한다() {
        var template =
            createTemplate.create(
                new CreateTemplateCommand.Auction(
                    "경매전",
                    2,
                    2,
                    300,
                    List.of("선수1", "선수2")
                )
            );

        Room created = createRoom.create(template.getId(), "호스트");
        joinRoom.join(created.getCode(), "게스트");
        startRoom.start(created.getCode());

        Room reloaded = rooms.findByCode(created.getCode()).orElseThrow();

        assertThat(reloaded.getLeaders()).hasSize(2);
        assertThat(reloaded.getStatus()).isEqualTo(RoomStatus.IN_PROGRESS);
        assertThat(reloaded.getCurrentAuctionRound()).isEqualTo(1);
    }
}
