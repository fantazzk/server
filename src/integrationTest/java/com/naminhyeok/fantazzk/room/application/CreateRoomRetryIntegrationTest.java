package com.naminhyeok.fantazzk.room.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.naminhyeok.fantazzk.CoreException;
import com.naminhyeok.fantazzk.room.application.CreateRoom;
import com.naminhyeok.fantazzk.room.application.CreateRoomAttempt;
import com.naminhyeok.fantazzk.room.application.RoomCodeGenerator;
import com.naminhyeok.fantazzk.room.domain.Room;
import com.naminhyeok.fantazzk.room.domain.RoomErrorType;
import com.naminhyeok.fantazzk.room.domain.RoomMode;
import com.naminhyeok.fantazzk.room.domain.RoomPlayerId;
import com.naminhyeok.fantazzk.room.domain.RoomTemplateSpec;
import com.naminhyeok.fantazzk.room.domain.TeamLeaderId;
import com.naminhyeok.fantazzk.room.repository.Rooms;
import com.naminhyeok.fantazzk.template.TemplateFixture;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.aop.support.AopUtils;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.TestConstructor;
import org.springframework.test.context.transaction.TestTransaction;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

@SpringBootTest(
    properties = {
        "spring.datasource.url=jdbc:h2:mem:create-room-retry-test;DB_CLOSE_DELAY=-1",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.datasource.username=sa",
        "spring.datasource.password=",
        "spring.jpa.hibernate.ddl-auto=validate",
        "spring.liquibase.enabled=true",
        "sentry.enabled=false"
    }
)
@TestConstructor(autowireMode = TestConstructor.AutowireMode.ALL)
@RequiredArgsConstructor
class CreateRoomRetryIntegrationTest {
    private final TemplateFixture templateFixture;
    private final CreateRoom createRoom;
    private final CreateRoomAttempt createRoomAttempt;
    private final Rooms rooms;
    private final PlatformTransactionManager transactionManager;
    private final JdbcTemplate jdbcTemplate;
    private final DeterministicRoomCodeGenerator roomCodeGenerator;

    @BeforeEach
    void setUp() {
        inNewTransaction(() -> {
            jdbcTemplate.update("delete from room_team_leader");
            jdbcTemplate.update("delete from room_player");
            jdbcTemplate.update("delete from rooms");
            jdbcTemplate.update("delete from template_player");
            jdbcTemplate.update("delete from template");
        });
        roomCodeGenerator.reset();
    }

    @Test
    @Transactional
    void 바깥_트랜잭션_안에서도_중복_방코드_충돌_후_새_시도로_방_생성에_성공한다() {
        assertThat(TestTransaction.isActive()).isTrue();
        assertThat(TestTransaction.isFlaggedForRollback()).isTrue();
        TestTransaction.flagForCommit();
        assertThat(TestTransaction.isFlaggedForRollback()).isFalse();

        var template =
            templateFixture.createAuctionTemplateId(
                "경매전",
                2,
                2,
                300,
                List.of(
                    new TemplateFixture.PlayerSpec("선수1", "TOP"),
                    new TemplateFixture.PlayerSpec("선수2", "JUNGLE")
                )
            );

        inNewTransaction(() -> rooms.saveAndFlush(existingRoom("ROOM01")));
        roomCodeGenerator.reset("ROOM01", "ROOM02");

        Room created = createRoom.create(template, "호스트").room();

        assertThat(AopUtils.isAopProxy(createRoomAttempt)).isTrue();
        assertThat(TestTransaction.isActive()).isTrue();
        assertThat(TestTransaction.isFlaggedForRollback()).isFalse();
        assertThat(created.getCode()).isEqualTo("ROOM02");
        assertThat(rooms.findByCode("ROOM01")).isPresent();
        assertThat(rooms.findByCode("ROOM02")).get()
            .extracting(Room::getId, Room::getCode)
            .containsExactly(created.getId(), "ROOM02");

        TestTransaction.end();

        assertThat(TestTransaction.isActive()).isFalse();

        inNewTransaction(() -> {
            assertThat(rooms.findByCode("ROOM01")).isPresent();
            assertThat(rooms.findByCode("ROOM02")).get()
                .extracting(Room::getId, Room::getCode)
                .containsExactly(created.getId(), "ROOM02");
        });
    }

    @Test
    void 중복_방코드_충돌이_반복되면_방_코드_생성_실패를_반환한다() {
        var template =
            templateFixture.createAuctionTemplateId(
                "경매전",
                2,
                2,
                300,
                List.of(
                    new TemplateFixture.PlayerSpec("선수1", "TOP"),
                    new TemplateFixture.PlayerSpec("선수2", "JUNGLE")
                )
            );

        inNewTransaction(() -> rooms.saveAndFlush(existingRoom("ROOM01")));
        roomCodeGenerator.reset("ROOM01", "ROOM01", "ROOM01");

        assertThatThrownBy(() -> createRoom.create(template, "호스트"))
            .isInstanceOf(CoreException.class)
            .satisfies(ex -> assertThat(((CoreException) ex).getError()).isEqualTo(RoomErrorType.ROOM_CODE_GENERATION_FAILED));
    }

    private void inNewTransaction(Runnable action) {
        TransactionTemplate transactionTemplate = new TransactionTemplate(transactionManager);
        transactionTemplate.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
        transactionTemplate.executeWithoutResult(status -> action.run());
    }

    private Room existingRoom(String code) {
        return Room.createFromTemplate(
            code,
            new TeamLeaderId("existing-host"),
            "선점호스트",
            "existing-token",
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
            java.time.Instant.parse("2026-04-10T00:00:00Z")
        );
    }

    @TestConfiguration
    static class TestConfig {
        @Bean
        @Primary
        DeterministicRoomCodeGenerator roomCodeGenerator() {
            return new DeterministicRoomCodeGenerator();
        }
    }

    static class DeterministicRoomCodeGenerator implements RoomCodeGenerator {
        private final Deque<String> codes = new ArrayDeque<>();

        void reset(String... nextCodes) {
            codes.clear();
            codes.addAll(List.of(nextCodes));
        }

        @Override
        public String generate() {
            if (codes.isEmpty()) {
                throw new IllegalStateException("테스트용 방 코드가 설정되지 않았습니다");
            }
            return codes.removeFirst();
        }
    }
}
