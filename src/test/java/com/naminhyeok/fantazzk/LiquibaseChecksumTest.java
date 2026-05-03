package com.naminhyeok.fantazzk;

import static org.assertj.core.api.Assertions.assertThat;

import liquibase.change.CheckSum;
import liquibase.changelog.ChangeLogParameters;
import liquibase.changelog.ChangeSet;
import liquibase.parser.core.yaml.YamlChangeLogParser;
import liquibase.resource.ClassLoaderResourceAccessor;
import org.junit.jupiter.api.Test;

class LiquibaseChecksumTest {
    private static final String CHANGELOG = "db/changelog/db.changelog-master.yaml";
    private static final CheckSum PRODUCTION_ROOM_GAME_CLEANUP_CHECKSUM = CheckSum.parse("9:ab793f9c89fa3ff8af0afacad5e1391f");

    @Test
    void 운영에_적용된_room_game_cleanup_changeset_checksum을_유효하게_유지한다() throws Exception {
        ChangeSet changeSet = changeSet("13-room-game-cleanup", "codex");

        assertThat(changeSet.isCheckSumValid(PRODUCTION_ROOM_GAME_CLEANUP_CHECKSUM)).isTrue();
    }

    private ChangeSet changeSet(String id, String author) throws Exception {
        try (var resourceAccessor = new ClassLoaderResourceAccessor(getClass().getClassLoader())) {
            var changeLog = new YamlChangeLogParser().parse(CHANGELOG, new ChangeLogParameters(), resourceAccessor);
            return changeLog.getChangeSet(CHANGELOG, author, id);
        }
    }
}
