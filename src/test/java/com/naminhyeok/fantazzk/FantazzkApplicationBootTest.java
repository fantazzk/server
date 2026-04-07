package com.naminhyeok.fantazzk;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Clock;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

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
class FantazzkApplicationBootTest {
    @Autowired
    private Clock clock;

    @Test
    void 루트_애플리케이션이_최소_설정으로_부팅된다() {
        assertThat(clock).isNotNull();
    }
}
