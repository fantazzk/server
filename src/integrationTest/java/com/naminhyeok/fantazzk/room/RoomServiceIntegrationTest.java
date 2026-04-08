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
}
