package com.naminhyeok.fantazzk.bootstrap.template

import org.springframework.boot.SpringBootConfiguration
import org.springframework.boot.autoconfigure.EnableAutoConfiguration
import org.springframework.boot.runApplication
import java.util.TimeZone

@SpringBootConfiguration
@EnableAutoConfiguration
class TemplateApplication

fun main(args: Array<String>) {
    TimeZone.setDefault(TimeZone.getTimeZone("UTC"))
    runApplication<TemplateApplication>(*args)
}
