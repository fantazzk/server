package com.naminhyeok.fantazzk.template.api

import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Import

@Import(TemplateApiController::class, TemplateExceptionHandler::class)
@Configuration
class TemplateApiAutoConfiguration
