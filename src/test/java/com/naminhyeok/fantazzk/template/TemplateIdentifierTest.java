package com.naminhyeok.fantazzk.template;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.UUID;
import org.junit.jupiter.api.Test;

class TemplateIdentifierTest {

    @Test
    void uuidBackedTemplateIdCanBeBuiltFromUuidAndString() {
        UUID uuid = UUID.randomUUID();

        TemplateId fromUuid = TemplateId.from(uuid);
        TemplateId fromString = TemplateId.from(uuid.toString());

        assertThat(fromUuid).isEqualTo(fromString);
        assertThat(fromUuid.getValue()).isEqualTo(uuid);
        assertThat(fromString.toString()).isEqualTo(uuid.toString());
    }
}
