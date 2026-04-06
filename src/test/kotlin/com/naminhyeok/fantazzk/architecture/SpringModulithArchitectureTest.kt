package com.naminhyeok.fantazzk.architecture

import com.naminhyeok.fantazzk.FantazzkApplication
import com.tngtech.archunit.core.importer.ClassFileImporter
import com.tngtech.archunit.core.importer.ImportOption
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatCode
import org.junit.jupiter.api.Test
import org.springframework.context.annotation.Configuration
import org.springframework.modulith.core.ApplicationModules
import org.springframework.modulith.docs.Documenter
import java.nio.file.Files
import java.nio.file.Path
import kotlin.io.path.extension
import kotlin.io.path.isRegularFile
import kotlin.io.path.listDirectoryEntries
import kotlin.io.path.readText
import org.jmolecules.ddd.types.Repository as DddRepository

class SpringModulithArchitectureTest {
    private val modules = ApplicationModules.of(FantazzkApplication::class.java)

    @Test
    fun `스프링 모듈리스가 애플리케이션 모듈 구조를 검증한다`() {
        assertThatCode { modules.verify() }
            .doesNotThrowAnyException()
    }

    @Test
    fun `일반 설정 클래스는 더 이상 auto configuration 네이밍을 쓰지 않는다`() {
        val classes =
            ClassFileImporter()
                .withImportOption(ImportOption.DoNotIncludeTests())
                .importPackages("com.naminhyeok.fantazzk")

        val legacyNamedConfigurations =
            classes
                .filter { it.isAnnotatedWith(Configuration::class.java) }
                .map { it.fullName }
                .filter { it.endsWith("AutoConfiguration") }

        assertThat(legacyNamedConfigurations).isEmpty()
    }

    @Test
    fun `같은 모듈의 애플리케이션 서비스는 수동 이벤트 발행을 사용하지 않는다`() {
        val sourceRoot = Path.of("src/main/kotlin/com/naminhyeok/fantazzk")
        val forbiddenMarkers = listOf("ApplicationEventPublisher", "publishEvent(", "drainEvents(")

        val violatingFiles =
            Files.walk(sourceRoot).use { paths ->
                paths
                    .filter { Files.isRegularFile(it) && it.fileName.toString().endsWith(".kt") }
                    .toList()
                    .mapNotNull { path ->
                        val content = path.readText()
                        val isApplicationSource = path.toString().contains("/application/")

                        if (isApplicationSource && forbiddenMarkers.any(content::contains)) {
                            sourceRoot.relativize(path).toString()
                        } else {
                            null
                        }
                    }
            }

        assertThat(violatingFiles).isEmpty()
    }

    @Test
    fun `같은 모듈의 애플리케이션 서비스는 서비스 인터페이스 와 impl 접미사를 두지 않는다`() {
        val classes =
            ClassFileImporter()
                .withImportOption(ImportOption.DoNotIncludeTests())
                .importPackages("com.naminhyeok.fantazzk")

        val interfaceViolations =
            classes
                .filter { it.packageName.contains(".application") }
                .filter { it.isInterface }
                .filter {
                    it.simpleName.endsWith("Service") ||
                        it.simpleName.endsWith("Finder") ||
                        it.simpleName.endsWith("Executor")
                }
                .map { it.fullName }

        val implViolations =
            classes
                .filter { it.packageName.contains(".application") }
                .filter {
                    it.simpleName.endsWith("ServiceImpl") ||
                        it.simpleName.endsWith("FinderImpl") ||
                        it.simpleName.endsWith("ExecutorImpl")
                }.map { it.fullName }

        assertThat(interfaceViolations).isEmpty()
        assertThat(implViolations).isEmpty()
    }

    @Test
    fun `리포지토리 추상화는 jmolecules repository 타입을 구현한다`() {
        val classes =
            ClassFileImporter()
                .withImportOption(ImportOption.DoNotIncludeTests())
                .importPackages("com.naminhyeok.fantazzk")

        val repositories =
            classes
                .filter { it.packageName.contains(".repository") }
                .filter { it.isInterface }
                .filterNot { it.simpleName.endsWith("JpaStore") }
                .map { it.reflect() }

        assertThat(repositories).isNotEmpty()
        assertThat(repositories).allMatch { DddRepository::class.java.isAssignableFrom(it) }

        val buildScript = Path.of("build.gradle.kts").readText()
        assertThat(buildScript).contains("org.jmolecules:jmolecules-ddd")
        assertThat(buildScript).doesNotContain("org.jmolecules:kmolecules-ddd")
    }

    @Test
    fun `메인 소스는 더 이상 spring data jdbc 전용 타입을 사용하지 않는다`() {
        val sourceRoot = Path.of("src/main/kotlin/com/naminhyeok/fantazzk")
        val jdbcUsages =
            Files.walk(sourceRoot).use { paths ->
                paths
                    .filter { Files.isRegularFile(it) && it.fileName.toString().endsWith(".kt") }
                    .toList()
                    .mapNotNull { path ->
                        val content = path.readText()
                        if (
                            "org.springframework.data.jdbc" in content ||
                            "org.springframework.data.relational.core.mapping" in content ||
                            "AbstractJdbcConfiguration" in content
                        ) {
                            sourceRoot.relativize(path).toString()
                        } else {
                            null
                        }
                    }
            }

        assertThat(jdbcUsages).isEmpty()
    }

    @Test
    fun `빌드 설정은 JPA 전환 이후 메인 JDBC starter 와 불필요한 data jdbc 테스트 슬라이스를 제거한다`() {
        val buildScript = Path.of("build.gradle.kts").readText()

        assertThat(buildScript).doesNotContain("implementation(\"org.springframework.boot:spring-boot-starter-data-jdbc\")")
        assertThat(buildScript).doesNotContain("integrationTestImplementation(\"org.springframework.boot:spring-boot-data-jdbc-test\")")
        assertThat(buildScript).contains("integrationTestImplementation(\"org.springframework.boot:spring-boot-jdbc-test\")")
    }

    @Test
    fun `스프링 모듈리스 문서기가 모듈 다이어그램과 캔버스를 생성한다`() {
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
