package com.naminhyeok.fantazzk.architecture

import com.naminhyeok.fantazzk.FantazzkApplication
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatCode
import org.junit.jupiter.api.Test
import org.springframework.modulith.core.ApplicationModules
import org.springframework.modulith.docs.Documenter
import java.nio.file.Files
import java.nio.file.Path
import kotlin.io.path.extension
import kotlin.io.path.isRegularFile
import kotlin.io.path.listDirectoryEntries

class SpringModulithArchitectureTest {
    private val modules = ApplicationModules.of(FantazzkApplication::class.java)

    @Test
    fun `spring modulith verifies application module structure`() {
        assertThatCode { modules.verify() }
            .doesNotThrowAnyException()
    }

    @Test
    fun `spring modulith documenter writes module diagrams and canvases`() {
        val outputDirectory = Path.of("build/spring-modulith")
        Files.createDirectories(outputDirectory)

        Documenter(modules, outputDirectory.toString())
            .writeModulesAsPlantUml()
            .writeIndividualModulesAsPlantUml()
            .writeModuleCanvases()

        val generatedFiles = outputDirectory.listDirectoryEntries()

        assertThat(generatedFiles.any { it.isRegularFile() && it.extension == "puml" }).isTrue()
        assertThat(generatedFiles.any { it.isRegularFile() && it.extension == "adoc" }).isTrue()
    }
}
