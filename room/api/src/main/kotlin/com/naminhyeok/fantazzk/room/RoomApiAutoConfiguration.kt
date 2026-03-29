package com.naminhyeok.fantazzk.room

import org.springframework.boot.autoconfigure.AutoConfiguration
import org.springframework.context.annotation.Import

@Import(RoomApiController::class, RoomExceptionHandler::class)
@AutoConfiguration
class RoomApiAutoConfiguration
