package com.naminhyeok.fantazzk.room.domain;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import java.util.UUID;
import org.jmolecules.ddd.types.Identifier;

public record RoomId(UUID roomId) implements Identifier {
    @Converter(autoApply = true)
    static final class JpaConverter implements AttributeConverter<RoomId, UUID> {
        @Override
        public UUID convertToDatabaseColumn(RoomId attribute) {
            return attribute == null ? null : attribute.roomId();
        }

        @Override
        public RoomId convertToEntityAttribute(UUID dbData) {
            return dbData == null ? null : new RoomId(dbData);
        }
    }
}
