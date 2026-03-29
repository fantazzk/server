package com.naminhyeok.fantazzk.template

interface TemplateIdentity {
    companion object
    val templateId: Long
}

internal data class SimpleTemplateIdentity(override val templateId: Long) : TemplateIdentity

fun TemplateIdentity.Companion.of(templateId: Long): TemplateIdentity = SimpleTemplateIdentity(templateId)
