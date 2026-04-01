package com.naminhyeok.fantazzk.room.config

import org.springframework.core.convert.converter.Converter
import org.springframework.data.convert.ReadingConverter
import org.springframework.data.convert.WritingConverter

@WritingConverter
class EnumToStringConverter<T : Enum<T>>(
    private val enumType: Class<T>,
) : Converter<T, String> {
    override fun convert(source: T): String = source.name
}

@ReadingConverter
class StringToEnumConverter<T : Enum<T>>(
    private val enumType: Class<T>,
) : Converter<String, T> {
    override fun convert(source: String): T = java.lang.Enum.valueOf(enumType, source)
}
