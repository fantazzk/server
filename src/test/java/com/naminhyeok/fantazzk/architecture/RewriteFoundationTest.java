package com.naminhyeok.fantazzk.architecture;

import static org.assertj.core.api.Assertions.assertThat;

import com.naminhyeok.fantazzk.FantazzkApplication;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.modulith.Modulithic;

class RewriteFoundationTest {

    @Test
    void java_entrypoint_exists() {
        assertThat(Path.of("src/main/java/com/naminhyeok/fantazzk/FantazzkApplication.java"))
                .exists();
    }

    @Test
    void java_entrypoint_declares_bootstrap_contract() throws Exception {
        assertThat(FantazzkApplication.class.isAnnotationPresent(SpringBootApplication.class)).isTrue();
        assertThat(FantazzkApplication.class.isAnnotationPresent(Modulithic.class)).isTrue();
        assertThat(FantazzkApplication.class.getAnnotation(Modulithic.class).systemName())
                .isEqualTo("Fantazzk");

        String source = Files.readString(Path.of("src/main/java/com/naminhyeok/fantazzk/FantazzkApplication.java"));

        assertThat(source).contains("TimeZone.setDefault(TimeZone.getTimeZone(\"UTC\"))");
    }

    @Test
    void build_uses_bytebuddy_and_jmolecules() throws Exception {
        String build = Files.readString(Path.of("build.gradle.kts"));

        assertThat(build).contains("net.bytebuddy.byte-buddy-gradle-plugin");
        assertThat(build).contains("org.jmolecules:jmolecules-ddd:2.0.1");
        assertThat(build).contains("org.jmolecules.integrations:jmolecules-spring:1.6.0");
        assertThat(build).contains("org.jmolecules.integrations:jmolecules-jackson:1.6.0");
        assertThat(build).contains("org.jmolecules.integrations:jmolecules-bytebuddy-nodep:0.33.0");
    }

    @Test
    void repository_no_longer_uses_kotlin_main_entrypoint() {
        assertThat(Path.of("src/main/kotlin/com/naminhyeok/fantazzk/FantazzkApplication.kt"))
                .doesNotExist();
    }
}
