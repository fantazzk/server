package com.naminhyeok.fantazzk.room.dto

data class PagedResponse<T>(
    val results: List<T>,
    val paging: Paging,
) {
    data class Paging(
        val pageNumber: Int,
        val pageSize: Int,
        val hasNext: Boolean,
        val totalCount: Long,
    )
}
