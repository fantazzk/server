package com.naminhyeok.fantazzk.room.domain.shared;


import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import org.jmolecules.ddd.types.Identifier;

public record RoomPlayerId(int value) implements Identifier {
    public RoomPlayerId {
        if (value < 0) {
            throw new IllegalArgumentException("방 선수 식별자는 음수일 수 없습니다");
        }
    }

    @Converter(autoApply = true)
    public static final class JpaConverter implements AttributeConverter<RoomPlayerId, Integer> {
        @Override
        public Integer convertToDatabaseColumn(RoomPlayerId attribute) {
            return attribute == null ? null : attribute.value();
        }

        @Override
        public RoomPlayerId convertToEntityAttribute(Integer dbData) {
            return dbData == null ? null : new RoomPlayerId(dbData);
        }
    }
}
