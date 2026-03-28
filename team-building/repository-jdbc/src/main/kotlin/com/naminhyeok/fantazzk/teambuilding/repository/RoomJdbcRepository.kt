package com.naminhyeok.fantazzk.teambuilding.repository

import org.springframework.data.repository.CrudRepository

interface RoomJdbcRepository : CrudRepository<RoomEntity, Long> {
    fun findByCode(code: String): RoomEntity?
}
