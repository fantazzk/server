package com.naminhyeok.fantazzk.room

interface RoomPlayerProps {
    val roomId: Long
    val name: String
    val status: PlayerStatus
    val displayOrder: Int
}
