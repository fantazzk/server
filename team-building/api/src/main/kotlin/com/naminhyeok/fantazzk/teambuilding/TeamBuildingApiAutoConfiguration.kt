package com.naminhyeok.fantazzk.teambuilding

import com.naminhyeok.fantazzk.teambuilding.room.RoomApiController
import com.naminhyeok.fantazzk.teambuilding.template.TemplateApiController
import org.springframework.boot.autoconfigure.AutoConfiguration
import org.springframework.context.annotation.Import

@Import(TemplateApiController::class, RoomApiController::class, TeamBuildingExceptionHandler::class)
@AutoConfiguration
class TeamBuildingApiAutoConfiguration
