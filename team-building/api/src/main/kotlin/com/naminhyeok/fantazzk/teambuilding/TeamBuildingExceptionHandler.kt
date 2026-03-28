package com.naminhyeok.fantazzk.teambuilding

import com.naminhyeok.fantazzk.teambuilding.exception.RoomNotFoundException
import com.naminhyeok.fantazzk.teambuilding.exception.TemplateNotFoundException
import org.springframework.http.HttpStatus
import org.springframework.http.ProblemDetail
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice

@RestControllerAdvice(basePackageClasses = [TeamBuildingExceptionHandler::class])
class TeamBuildingExceptionHandler {
    @ExceptionHandler(TemplateNotFoundException::class, RoomNotFoundException::class)
    fun handleNotFound(ex: RuntimeException): ProblemDetail =
        ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, ex.message ?: "Not found")

    @ExceptionHandler(IllegalArgumentException::class)
    fun handleBadRequest(ex: IllegalArgumentException): ProblemDetail =
        ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, ex.message ?: "Bad request")

    @ExceptionHandler(IllegalStateException::class)
    fun handleIllegalState(ex: IllegalStateException): ProblemDetail =
        ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, ex.message ?: "Invalid state")
}
