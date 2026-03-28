package com.naminhyeok.fantazzk.bootstrap.teambuilding

import org.springframework.boot.SpringBootConfiguration
import org.springframework.boot.autoconfigure.EnableAutoConfiguration
import org.springframework.boot.runApplication
import java.util.TimeZone

@SpringBootConfiguration
@EnableAutoConfiguration
class TeamBuildingApplication

fun main(args: Array<String>) {
    TimeZone.setDefault(TimeZone.getTimeZone("UTC"))
    runApplication<TeamBuildingApplication>(*args)
}
