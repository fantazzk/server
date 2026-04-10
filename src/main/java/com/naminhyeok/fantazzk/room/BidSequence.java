package com.naminhyeok.fantazzk.room;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import org.jmolecules.ddd.types.ValueObject;

record BidSequence(int value) implements ValueObject {
    BidSequence {
        if (value < 1) {
            throw new IllegalArgumentException("입찰 순번은 1 이상이어야 합니다");
        }
    }

    @Converter(autoApply = true)
    static final class JpaConverter implements AttributeConverter<BidSequence, Integer> {
        @Override
        public Integer convertToDatabaseColumn(BidSequence attribute) {
            return attribute == null ? null : attribute.value();
        }

        @Override
        public BidSequence convertToEntityAttribute(Integer dbData) {
            return dbData == null ? null : new BidSequence(dbData);
        }
    }
}
