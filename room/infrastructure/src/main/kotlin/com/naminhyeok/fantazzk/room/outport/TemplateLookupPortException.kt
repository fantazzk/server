package com.naminhyeok.fantazzk.room.outport

sealed class TemplateLookupPortException(message: String) : RuntimeException(message) {
    class NotFound(
        val templateId: Long,
    ) : TemplateLookupPortException("템플릿을 찾을 수 없습니다")
}
