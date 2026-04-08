package com.naminhyeok.fantazzk.architecture;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Set;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Test;

class PublishedContractStructureTest {
    @Test
    void room_루트_패키지는_공개_계약을_노출하지_않는다() {
        assertThat(classesInExactPackage("com.naminhyeok.fantazzk.room")).isEmpty();
    }

    @Test
    void template_루트_패키지는_게시된_계약만_노출한다() {
        assertThat(classesInExactPackage("com.naminhyeok.fantazzk.template"))
            .containsExactly("TemplateCatalog");
    }

    private Set<String> classesInExactPackage(String packageName) {
        Path sourceDirectory = Path.of("src/main/java").resolve(packageName.replace('.', '/'));
        try (var files = Files.list(sourceDirectory)) {
            return files
                .filter(path -> path.getFileName().toString().endsWith(".java"))
                .map(path -> path.getFileName().toString().replaceFirst("\\.java$", ""))
                .collect(Collectors.toSet());
        } catch (IOException ex) {
            throw new UncheckedIOException(ex);
        }
    }
}
