package com.naminhyeok.fantazzk.architecture;

import static org.assertj.core.api.Assertions.assertThat;

import java.nio.file.Path;
import java.util.List;
import org.junit.jupiter.api.Test;

class JavaRewriteLayoutTest {

    private static final List<Path> REQUIRED_PACKAGE_INFOS = List.of(
            Path.of("src/main/java/com/naminhyeok/fantazzk/template/package-info.java"),
            Path.of("src/main/java/com/naminhyeok/fantazzk/template/domain/package-info.java"),
            Path.of("src/main/java/com/naminhyeok/fantazzk/template/application/package-info.java"),
            Path.of("src/main/java/com/naminhyeok/fantazzk/template/repository/package-info.java"),
            Path.of("src/main/java/com/naminhyeok/fantazzk/template/web/package-info.java"),
            Path.of("src/main/java/com/naminhyeok/fantazzk/room/package-info.java"),
            Path.of("src/main/java/com/naminhyeok/fantazzk/room/domain/package-info.java"),
            Path.of("src/main/java/com/naminhyeok/fantazzk/room/application/package-info.java"),
            Path.of("src/main/java/com/naminhyeok/fantazzk/room/repository/package-info.java"),
            Path.of("src/main/java/com/naminhyeok/fantazzk/room/web/package-info.java"));

    @Test
    void required_package_info_files_exist_for_modulith_roles() {
        assertThat(REQUIRED_PACKAGE_INFOS).allSatisfy(path -> assertThat(path).exists());
    }

    @Test
    void legacy_module_metadata_types_are_removed() {
        assertThat(Path.of("src/main/kotlin/com/naminhyeok/fantazzk/template/TemplateModuleMetadata.kt"))
                .doesNotExist();
        assertThat(Path.of("src/main/kotlin/com/naminhyeok/fantazzk/room/RoomModuleMetadata.kt"))
                .doesNotExist();
    }
}
