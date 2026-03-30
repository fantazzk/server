package com.naminhyeok.fantazzk.room.exception

class RoomTemplateNotFoundException : RoomException(
    errorCode = "TEMPLATE_NOT_FOUND",
    message = "템플릿을 찾을 수 없습니다",
)
