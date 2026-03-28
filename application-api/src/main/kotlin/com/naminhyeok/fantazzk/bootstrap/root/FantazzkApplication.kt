package com.naminhyeok.fantazzk.bootstrap.root

import org.springframework.boot.SpringBootConfiguration
import org.springframework.boot.autoconfigure.EnableAutoConfiguration
import org.springframework.boot.runApplication
import java.util.TimeZone

@SpringBootConfiguration
@EnableAutoConfiguration
class FantazzkApplication

fun main(args: Array<String>) {
    TimeZone.setDefault(TimeZone.getTimeZone("UTC"))
    runApplication<FantazzkApplication>(*args)
}
