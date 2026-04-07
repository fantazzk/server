package com.naminhyeok.fantazzk.architecture;

import static org.assertj.core.api.Assertions.assertThatCode;

import com.naminhyeok.fantazzk.FantazzkApplication;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.springframework.modulith.core.ApplicationModules;
import org.springframework.modulith.docs.Documenter;

class ModulithStructureTest {
    @Test
    void 현재_자바_구조에서_모듈_검증이_통과한다() {
        assertThatCode(() -> ApplicationModules.of(FantazzkApplication.class).verify())
            .doesNotThrowAnyException();
    }

    @Test
    void 모듈_문서화를_생성할_수_있다() throws Exception {
        var modules = ApplicationModules.of(FantazzkApplication.class);
        Path outputDirectory = Path.of("build/spring-modulith");
        Files.createDirectories(outputDirectory);

        new Documenter(modules, outputDirectory.toString())
            .writeModulesAsPlantUml()
            .writeIndividualModulesAsPlantUml()
            .writeModuleCanvases();
    }
}
