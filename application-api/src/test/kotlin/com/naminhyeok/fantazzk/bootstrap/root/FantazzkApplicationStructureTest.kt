package com.naminhyeok.fantazzk.bootstrap.root

import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.springframework.boot.SpringBootConfiguration
import org.springframework.boot.autoconfigure.EnableAutoConfiguration
import org.springframework.boot.autoconfigure.SpringBootApplication

class FantazzkApplicationStructureTest {
    @Test
    fun `root launcher should avoid spring boot application scanning`() {
        assertNotNull(FantazzkApplication::class.java.getAnnotation(SpringBootConfiguration::class.java))
        assertNotNull(FantazzkApplication::class.java.getAnnotation(EnableAutoConfiguration::class.java))
        assertFalse(FantazzkApplication::class.java.isAnnotationPresent(SpringBootApplication::class.java))
    }

    @Test
    fun `root application auto configuration should be registered explicitly`() {
        val imports =
            FantazzkApplication::class.java.classLoader
                .getResourceAsStream("META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports")
                ?.bufferedReader()
                ?.readText()

        assertNotNull(imports)
        assertTrue(imports!!.contains("com.naminhyeok.fantazzk.bootstrap.root.FantazzkApplicationAutoConfiguration"))
    }
}
