package com.naminhyeok.fantazzk.bootstrap.root

import com.naminhyeok.fantazzk.FantazzkApplication
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.modulith.Modulithic
import java.nio.file.Files
import java.nio.file.Path

class FantazzkApplicationStructureTest {
    @Test
    fun `루트 런처는 spring boot application scanning 을 사용한다`() {
        assertTrue(FantazzkApplication::class.java.isAnnotationPresent(SpringBootApplication::class.java))
    }

    @Test
    fun `루트 런처는 modulith 메타데이터를 선언한다`() {
        assertTrue(FantazzkApplication::class.java.isAnnotationPresent(Modulithic::class.java))
    }

    @Test
    fun `레거시 auto configuration imports 리소스는 더 이상 필요하지 않다`() {
        assertTrue(
            Files.notExists(Path.of("src/main/resources/META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports")),
        )
    }

    @Test
    fun `api wrapper 설정은 제거되고 패키지 스캐닝으로 대체된다`() {
        assertThatThrownBy {
            Class.forName("com.naminhyeok.fantazzk.room.api.RoomApiAutoConfiguration")
        }.isInstanceOf(ClassNotFoundException::class.java)

        assertThatThrownBy {
            Class.forName("com.naminhyeok.fantazzk.template.api.TemplateApiAutoConfiguration")
        }.isInstanceOf(ClassNotFoundException::class.java)
    }
}
