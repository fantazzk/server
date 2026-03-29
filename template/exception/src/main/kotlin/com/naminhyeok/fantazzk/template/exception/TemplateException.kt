package com.naminhyeok.fantazzk.template.exception

sealed class TemplateException(
    val errorCode: String,
    message: String,
) : RuntimeException(message) {
    class TemplateNotFoundException : TemplateException(
        errorCode = "TEMPLATE_NOT_FOUND",
        message = "템플릿을 찾을 수 없습니다",
    )
}
