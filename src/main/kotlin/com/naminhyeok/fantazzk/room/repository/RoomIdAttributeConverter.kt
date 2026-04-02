package com.naminhyeok.fantazzk.room.repository

import com.naminhyeok.fantazzk.room.RoomId
import jakarta.persistence.AttributeConverter
import jakarta.persistence.Converter

@Converter(autoApply = true)
class RoomIdAttributeConverter : AttributeConverter<RoomId, Long> {
    override fun convertToDatabaseColumn(attribute: RoomId?): Long? = attribute?.value

    override fun convertToEntityAttribute(dbData: Long?): RoomId? = dbData?.let(::RoomId)
}
