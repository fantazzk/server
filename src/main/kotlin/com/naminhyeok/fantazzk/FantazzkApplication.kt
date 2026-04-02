package com.naminhyeok.fantazzk

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.modulith.Modulithic
import java.util.TimeZone

@SpringBootApplication
@Modulithic(systemName = "Fantazzk")
class FantazzkApplication

fun main(args: Array<String>) {
    TimeZone.setDefault(TimeZone.getTimeZone("UTC"))
    runApplication<FantazzkApplication>(*args)
}
