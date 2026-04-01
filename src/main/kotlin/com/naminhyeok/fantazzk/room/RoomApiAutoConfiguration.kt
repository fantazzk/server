package com.naminhyeok.fantazzk.room.api

import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Import

@Import(RoomApiController::class, RoomExceptionHandler::class)
@Configuration
class RoomApiAutoConfiguration
