package com.naminhyeok.fantazzk.room;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import org.jmolecules.ddd.types.Identifier;

record TeamLeaderId(String value) implements Identifier {
    TeamLeaderId {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("팀장 식별자는 비어 있을 수 없습니다");
        }
    }

    @Converter(autoApply = true)
    static final class JpaConverter implements AttributeConverter<TeamLeaderId, String> {
        @Override
        public String convertToDatabaseColumn(TeamLeaderId attribute) {
            return attribute == null ? null : attribute.value();
        }

        @Override
        public TeamLeaderId convertToEntityAttribute(String dbData) {
            return dbData == null ? null : new TeamLeaderId(dbData);
        }
    }
}
