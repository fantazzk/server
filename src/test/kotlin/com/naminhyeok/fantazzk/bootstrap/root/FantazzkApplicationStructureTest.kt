package com.naminhyeok.fantazzk.bootstrap.root

import com.naminhyeok.fantazzk.FantazzkApplication
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.springframework.boot.autoconfigure.SpringBootApplication
import java.nio.file.Files
import java.nio.file.Path

class FantazzkApplicationStructureTest {
    @Test
    fun `root launcher should use spring boot application scanning`() {
        assertTrue(FantazzkApplication::class.java.isAnnotationPresent(SpringBootApplication::class.java))
    }

    @Test
    fun `legacy auto configuration imports resource is no longer required`() {
        assertTrue(
            Files.notExists(Path.of("src/main/resources/META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports")),
        )
    }
}
