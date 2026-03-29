package com.naminhyeok.fantazzk.template

import org.springframework.boot.autoconfigure.AutoConfiguration
import org.springframework.context.annotation.Import

@Import(TemplateApiController::class, TemplateExceptionHandler::class)
@AutoConfiguration
class TemplateApiAutoConfiguration
