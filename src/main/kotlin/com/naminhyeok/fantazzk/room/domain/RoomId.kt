package com.naminhyeok.fantazzk.room.domain

import org.jmolecules.ddd.types.Identifier

data class RoomId(
    val value: Long,
) : Identifier {
    init {
        require(value > 0L) { "RoomId는 1 이상이어야 합니다" }
    }

    companion object {
        fun from(value: Long): RoomId = RoomId(value)

        fun fromOrNull(value: Long?): RoomId? = value?.takeIf { it != 0L }?.let(::from)
    }
}
