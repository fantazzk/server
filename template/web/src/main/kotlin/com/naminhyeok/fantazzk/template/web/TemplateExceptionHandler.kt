package com.naminhyeok.fantazzk.template.web

import com.naminhyeok.fantazzk.template.exception.TemplateException
import com.naminhyeok.fantazzk.template.web.dto.ApiResponse
import org.slf4j.LoggerFactory
import org.springframework.boot.logging.LogLevel
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.HttpStatusCode
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import org.springframework.web.context.request.WebRequest
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler

@RestControllerAdvice(basePackageClasses = [TemplateExceptionHandler::class])
class TemplateExceptionHandler : ResponseEntityExceptionHandler() {
    private val log = LoggerFactory.getLogger(javaClass)

    override fun handleExceptionInternal(
        ex: Exception,
        body: Any?,
        headers: HttpHeaders,
        status: HttpStatusCode,
        request: WebRequest,
    ): ResponseEntity<Any> {
        val errorCode =
            when (ex) {
                is MethodArgumentNotValidException -> "VALIDATION_ERROR"
                else -> "REQUEST_ERROR"
            }
        val reason =
            when (ex) {
                is MethodArgumentNotValidException -> "요청 값이 올바르지 않습니다"
                else -> ex.message ?: "Bad request"
            }
        val data =
            when (ex) {
                is MethodArgumentNotValidException ->
                    ex.bindingResult.fieldErrors.groupBy({ it.field }, { it.defaultMessage ?: "" })
                else -> null
            }
        log.warn("{}: {}", errorCode, reason)
        return ResponseEntity.status(status)
            .headers(headers)
            .body(ApiResponse.error(status.value(), errorCode, reason, data))
    }

    @ExceptionHandler(TemplateException::class)
    fun handle(ex: TemplateException): ResponseEntity<ApiResponse<Nothing>> {
        val (status, logLevel) =
            when (ex) {
                is TemplateException.TemplateNotFoundException -> HttpStatus.NOT_FOUND to LogLevel.WARN
                is TemplateException.TemplateInvalidException -> HttpStatus.CONFLICT to LogLevel.WARN
            }
        log(logLevel, "TemplateException", ex)
        return ResponseEntity.status(status)
            .body(ApiResponse.error(status.value(), ex.errorCode, ex.message ?: ""))
    }

    @ExceptionHandler(IllegalArgumentException::class)
    fun handleBadRequest(ex: IllegalArgumentException): ResponseEntity<ApiResponse<Nothing>> {
        val status = HttpStatus.BAD_REQUEST
        log.warn("BadRequest: {}", ex.message)
        return ResponseEntity.status(status)
            .body(ApiResponse.error(status.value(), "BAD_REQUEST", ex.message ?: "Bad request"))
    }

    @ExceptionHandler(IllegalStateException::class)
    fun handleIllegalState(ex: IllegalStateException): ResponseEntity<ApiResponse<Nothing>> {
        val status = HttpStatus.CONFLICT
        log.warn("IllegalState: {}", ex.message)
        return ResponseEntity.status(status)
            .body(ApiResponse.error(status.value(), "INVALID_STATE", ex.message ?: "Invalid state"))
    }

    @ExceptionHandler(NoSuchElementException::class)
    fun handleNoSuchElement(ex: NoSuchElementException): ResponseEntity<ApiResponse<Nothing>> {
        val status = HttpStatus.NOT_FOUND
        log.warn("NoSuchElement: {}", ex.message)
        return ResponseEntity.status(status)
            .body(ApiResponse.error(status.value(), "NOT_FOUND", ex.message ?: "Not found"))
    }

    @ExceptionHandler(Exception::class)
    fun handleUnexpected(ex: Exception): ResponseEntity<ApiResponse<Nothing>> {
        val status = HttpStatus.INTERNAL_SERVER_ERROR
        log.error("Unexpected: {}", ex.message, ex)
        return ResponseEntity.status(status)
            .body(ApiResponse.error(status.value(), "INTERNAL_ERROR", "예기치 못한 에러가 발생했습니다"))
    }

    private fun log(
        level: LogLevel,
        prefix: String,
        ex: Exception,
    ) {
        when (level) {
            LogLevel.ERROR -> log.error("{}: {}", prefix, ex.message, ex)
            LogLevel.WARN -> log.warn("{}: {}", prefix, ex.message)
            else -> log.info("{}: {}", prefix, ex.message)
        }
    }
}
