package com.naminhyeok.fantazzk.template.repository

import com.naminhyeok.fantazzk.template.TemplateId
import jakarta.persistence.AttributeConverter
import jakarta.persistence.Converter

@Converter(autoApply = true)
class TemplateIdAttributeConverter : AttributeConverter<TemplateId, Long> {
    override fun convertToDatabaseColumn(attribute: TemplateId?): Long? = attribute?.value

    override fun convertToEntityAttribute(dbData: Long?): TemplateId? = dbData?.let(::TemplateId)
}
