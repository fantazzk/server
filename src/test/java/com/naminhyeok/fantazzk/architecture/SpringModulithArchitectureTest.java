package com.naminhyeok.fantazzk.architecture;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;

import com.naminhyeok.fantazzk.FantazzkApplication;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.springframework.modulith.core.ApplicationModules;
import org.springframework.modulith.docs.Documenter;

class SpringModulithArchitectureTest {

    private final ApplicationModules modules = ApplicationModules.of(FantazzkApplication.class);

    @Test
    void application_modules_verify() {
        assertThatCode(modules::verify).doesNotThrowAnyException();
    }

    @Test
    void documenter_generates_plantuml_and_canvas() throws Exception {
        Path output = Path.of("build/spring-modulith");
        recreateDirectory(output);

        new Documenter(modules, output.toString())
                .writeModulesAsPlantUml()
                .writeIndividualModulesAsPlantUml()
                .writeModuleCanvases();

        try (Stream<Path> generatedFiles = Files.walk(output)) {
            assertThat(generatedFiles.anyMatch(path -> path.toString().endsWith(".puml"))).isTrue();
        }

        try (Stream<Path> generatedFiles = Files.walk(output)) {
            assertThat(generatedFiles.anyMatch(path -> path.toString().endsWith(".adoc"))).isTrue();
        }
    }

    private static void recreateDirectory(Path output) throws IOException {
        if (Files.exists(output)) {
            try (Stream<Path> paths = Files.walk(output)) {
                paths.sorted(Comparator.reverseOrder())
                        .forEach(path -> {
                            try {
                                Files.delete(path);
                            } catch (IOException exception) {
                                throw new RuntimeException(exception);
                            }
                        });
            }
        }

        Files.createDirectories(output);
    }
}
