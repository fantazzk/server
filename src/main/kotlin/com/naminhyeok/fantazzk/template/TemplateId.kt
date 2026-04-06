package com.naminhyeok.fantazzk.template

import org.jmolecules.ddd.types.Identifier
import java.util.UUID

data class TemplateId(
    val value: UUID,
) : Identifier {
    constructor(value: Long) : this(UUID(0L, value))

    companion object {
        @JvmStatic
        fun of(value: String): TemplateId = TemplateId(UUID.fromString(value))

        @JvmStatic
        fun of(value: UUID): TemplateId = TemplateId(value)

        @JvmStatic
        fun of(value: Long): TemplateId = TemplateId(value)

        @JvmStatic
        fun newId(): TemplateId = TemplateId(UUID.randomUUID())
    }
}
