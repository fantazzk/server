package com.naminhyeok.fantazzk.room.exception

sealed class RoomException(
    val errorCode: String,
    message: String,
) : RuntimeException(message) {
    class RoomNotFoundException : RoomException(
        errorCode = "ROOM_NOT_FOUND",
        message = "방을 찾을 수 없습니다",
    )

    class TeamLeaderNotFoundException : RoomException(
        errorCode = "ROOM_TEAM_LEADER_NOT_FOUND",
        message = "팀장을 찾을 수 없습니다",
    )
}
