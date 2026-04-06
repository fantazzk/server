package com.naminhyeok.fantazzk.room.repository;

import com.naminhyeok.fantazzk.room.RoomCode;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
final class RoomCodeAttributeConverter implements AttributeConverter<RoomCode, String> {
    @Override
    public String convertToDatabaseColumn(RoomCode attribute) {
        return attribute == null ? null : attribute.getValue();
    }

    @Override
    public RoomCode convertToEntityAttribute(String dbData) {
        return dbData == null ? null : RoomCode.of(dbData);
    }
}
