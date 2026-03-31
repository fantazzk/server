package com.naminhyeok.fantazzk.template.api

interface TemplateLookup {
    fun get(templateId: Long): TemplateView
}
