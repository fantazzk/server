package com.naminhyeok.fantazzk.room;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "방 생성/참가 직후 반환되는 세션 응답")
record RoomSessionResponse(
    @Schema(description = "현재 로비 스냅샷")
    RoomViewResponse room,
    @Schema(description = "현재 사용자의 방 세션 정보. FE는 이 값을 저장해야 이후 액션을 수행할 수 있습니다.")
    TeamLeaderSessionResponse teamLeaderSession
) {
    static RoomSessionResponse from(RoomSessionResult result) {
        return from(result.room(), result.leader());
    }

    static RoomSessionResponse fromHost(Room room) {
        RoomTeamLeader host = room.getLeaders().stream()
            .filter(leader -> leader.getId().equals(room.getHostLeaderId()))
            .findFirst()
            .orElseThrow();
        return from(room, host);
    }

    static RoomSessionResponse from(Room room, RoomTeamLeader leader) {
        return new RoomSessionResponse(
            RoomViewResponse.from(room),
            TeamLeaderSessionResponse.from(room, leader)
        );
    }
}
