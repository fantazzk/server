package com.naminhyeok.fantazzk;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Clock;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestConstructor;

@SpringBootTest(
    properties = {
        "spring.datasource.url=jdbc:h2:mem:boot-test;DB_CLOSE_DELAY=-1",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.datasource.username=sa",
        "spring.datasource.password=",
        "spring.liquibase.enabled=false",
        "sentry.enabled=false"
    }
)
@TestConstructor(autowireMode = TestConstructor.AutowireMode.ALL)
@RequiredArgsConstructor
class FantazzkApplicationBootTest {
    private final Clock clock;

    @Test
    void 루트_애플리케이션이_최소_설정으로_부팅된다() {
        assertThat(clock).isNotNull();
    }
}
