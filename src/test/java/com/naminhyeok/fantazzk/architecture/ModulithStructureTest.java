package com.naminhyeok.fantazzk.architecture;

import static org.assertj.core.api.Assertions.assertThatCode;

import com.naminhyeok.fantazzk.FantazzkApplication;
import org.junit.jupiter.api.Test;
import org.springframework.modulith.core.ApplicationModules;

class ModulithStructureTest {
    @Test
    void 현재_자바_구조에서_모듈_검증이_통과한다() {
        assertThatCode(() -> ApplicationModules.of(FantazzkApplication.class).verify())
            .doesNotThrowAnyException();
    }
}
