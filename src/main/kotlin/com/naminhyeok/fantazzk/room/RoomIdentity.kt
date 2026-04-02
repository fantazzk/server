package com.naminhyeok.fantazzk.room

interface RoomIdentity {
    companion object

    val roomId: Long
}

internal data class SimpleRoomIdentity(override val roomId: Long) : RoomIdentity

fun RoomIdentity.Companion.of(roomId: Long): RoomIdentity = SimpleRoomIdentity(roomId)
