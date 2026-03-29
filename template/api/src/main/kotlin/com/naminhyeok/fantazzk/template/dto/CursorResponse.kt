package com.naminhyeok.fantazzk.template.dto

data class CursorResponse<T>(
    val results: List<T>,
    val next: String?,
    val totalCount: Long?,
)
