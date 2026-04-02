package com.naminhyeok.fantazzk.room.dto

import com.naminhyeok.fantazzk.room.Room
import com.naminhyeok.fantazzk.room.RoomStatus
import com.naminhyeok.fantazzk.room.RoomTeamLeader
import com.naminhyeok.fantazzk.room.query.RoomView
import io.swagger.v3.oas.annotations.media.ArraySchema
import io.swagger.v3.oas.annotations.media.Schema

@Schema(description = "방 조회 및 상태 변경 API 의 성공 응답 payload 입니다.")
data class RoomResponse(
    @field:Schema(description = "발급된 6자리 방 코드입니다.", example = "ROOM01")
    val code: String,
    @field:Schema(description = "현재 방 상태입니다.", example = "WAITING")
    val status: RoomStatus,
    @field:ArraySchema(
        arraySchema = Schema(description = "현재 방에 참가한 팀장 목록입니다."),
        schema = Schema(implementation = TeamLeaderResponse::class),
    )
    val teamLeaders: List<TeamLeaderResponse>,
) {
    companion object {
        fun from(
            room: Room,
            leaders: List<RoomTeamLeader>,
        ): RoomResponse =
            RoomResponse(
                code = room.code,
                status = room.status,
                teamLeaders = leaders.map { TeamLeaderResponse(it.teamLeaderId, it.nickname, it.remainingBudget) },
            )

        fun from(view: RoomView): RoomResponse =
            RoomResponse(
                code = view.code,
                status = view.status,
                teamLeaders = view.teamLeaders.map { TeamLeaderResponse(it.id, it.nickname, it.remainingBudget) },
            )
    }
}
