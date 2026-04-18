package com.naminhyeok.fantazzk.room.domain.shared;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import org.jmolecules.ddd.types.Identifier;

public record TeamLeaderId(String value) implements Identifier {
    public TeamLeaderId {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("팀장 식별자는 비어 있을 수 없습니다");
        }
    }

    @Converter(autoApply = true)
    public static final class JpaConverter implements AttributeConverter<TeamLeaderId, String> {
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
