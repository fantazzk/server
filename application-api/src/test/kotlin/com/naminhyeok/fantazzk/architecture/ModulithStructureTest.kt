package com.naminhyeok.fantazzk.architecture

import com.naminhyeok.fantazzk.bootstrap.root.FantazzkApplication
import org.assertj.core.api.Assertions.assertThatCode
import org.junit.jupiter.api.Test
import org.springframework.modulith.core.ApplicationModules

class ModulithStructureTest {
    @Test
    fun `room과 template 모듈 구성이 검증된다`() {
        assertThatCode {
            ApplicationModules.of(FantazzkApplication::class.java).verify()
        }.doesNotThrowAnyException()
    }
}
