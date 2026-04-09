package com.naminhyeok.fantazzk;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Clock;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestConstructor;

@SpringBootTest(
    properties = {
        "spring.profiles.active=openapi",
        "server.port=0"
    }
)
@TestConstructor(autowireMode = TestConstructor.AutowireMode.ALL)
@RequiredArgsConstructor
class FantazzkApplicationBootTest {
    private final Clock clock;

    @Test
    void openapi_프로필로_애플리케이션이_부팅된다() {
        assertThat(clock).isNotNull();
    }
}
