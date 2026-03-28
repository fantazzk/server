package com.naminhyeok.fantazzk.teambuilding

import com.naminhyeok.fantazzk.teambuilding.repository.RoomRepository
import com.naminhyeok.fantazzk.teambuilding.repository.TemplateRepository
import org.springframework.boot.autoconfigure.AutoConfiguration
import org.springframework.context.annotation.Bean

@AutoConfiguration
class TeamBuildingAutoConfiguration {
    @Bean
    fun templateService(templateRepository: TemplateRepository): TemplateService = TemplateServiceImpl(templateRepository)

    @Bean
    fun roomService(roomRepository: RoomRepository): RoomService = RoomServiceImpl(roomRepository)
}
