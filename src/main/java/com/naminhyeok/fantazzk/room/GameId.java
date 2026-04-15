package com.naminhyeok.fantazzk.room;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import java.util.Objects;
import java.util.UUID;
import org.jmolecules.ddd.types.Identifier;

record GameId(UUID gameId) implements Identifier {
    GameId {
        Objects.requireNonNull(gameId, "gameId must not be null");
    }

    @Converter(autoApply = true)
    static final class JpaConverter implements AttributeConverter<GameId, UUID> {
        @Override
        public UUID convertToDatabaseColumn(GameId attribute) {
            return attribute == null ? null : attribute.gameId();
        }

        @Override
        public GameId convertToEntityAttribute(UUID dbData) {
            return dbData == null ? null : new GameId(dbData);
        }
    }
}
