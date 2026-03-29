package com.naminhyeok.fantazzk.room

internal object RoomOpenApiDocs {
    const val TAG_DESCRIPTION = "방 생성, 참가, 시작, 경매, 드래프트 진행을 위한 API입니다."

    const val CREATE_DESCRIPTION =
        "템플릿을 기반으로 새 방을 생성합니다.\n" +
            "- 생성 직후 방 상태는 WAITING 입니다.\n" +
            "- 호스트는 첫 번째 팀장으로 등록됩니다.\n" +
            "- success 에는 생성된 방 코드와 현재 팀장 목록이 포함됩니다."

    const val GET_DESCRIPTION =
        "방 코드로 현재 방 상태를 조회합니다.\n" +
            "- success.status 로 WAITING, IN_PROGRESS, COMPLETED 중 현재 상태를 확인할 수 있습니다.\n" +
            "- success.teamLeaders 에 현재 참가한 팀장 목록이 포함됩니다."

    const val JOIN_DESCRIPTION =
        "대기 중인 방에 팀장으로 참가합니다.\n" +
            "- 현재 구현에서는 WAITING 상태의 방에서만 참가할 수 있습니다.\n" +
            "- 팀장 수가 teamCount 미만일 때만 참가할 수 있습니다.\n" +
            "- 성공하면 success.teamLeaders 목록에 새 팀장이 반영됩니다."

    const val START_DESCRIPTION =
        "대기 중인 방을 시작합니다.\n" +
            "- 현재 구현에서는 WAITING 상태에서만 시작할 수 있습니다.\n" +
            "- 팀장 수가 room.teamCount 와 같아야 시작할 수 있습니다.\n" +
            "- 성공하면 success.status 값이 IN_PROGRESS 로 변경됩니다."

    const val BID_DESCRIPTION =
        "진행 중인 경매 방에서 현재 라운드에 입찰합니다.\n" +
            "- IN_PROGRESS 상태이면서 경매 모드인 방에서만 호출할 수 있습니다.\n" +
            "- amount 는 현재 최고가보다 커야 합니다.\n" +
            "- teamLeaderId 는 RoomResponse.teamLeaders[].id 값 중 하나여야 합니다."

    const val SETTLE_DESCRIPTION =
        "진행 중인 경매 라운드를 정산합니다.\n" +
            "- IN_PROGRESS 상태이면서 경매 모드인 방에서만 호출할 수 있습니다.\n" +
            "- 낙찰이 있으면 선수 배정과 예산 차감이 반영됩니다.\n" +
            "- 모든 배정이 끝나면 success.status 가 COMPLETED 로 바뀔 수 있습니다."

    const val PICK_DESCRIPTION =
        "진행 중인 드래프트 방에서 현재 턴의 팀장이 선수를 지명합니다.\n" +
            "- IN_PROGRESS 상태이면서 드래프트 모드인 방에서만 호출할 수 있습니다.\n" +
            "- 현재 턴이 아닌 팀장이 호출하면 409를 반환합니다.\n" +
            "- 성공하면 success 상태가 최신 방 스냅샷으로 갱신됩니다."

    const val ROOM_CODE_PARAMETER = "조회 또는 진행할 6자리 방 코드입니다."
    const val TEAM_LEADER_ID_PARAMETER = "RoomResponse.teamLeaders[].id 로 받은 팀장 식별자입니다."

    const val CREATE_ROOM_REQUEST_EXAMPLE = """{"templateId":1,"hostNickname":"호스트"}"""
    const val JOIN_ROOM_REQUEST_EXAMPLE = """{"nickname":"참가자"}"""
    const val PLACE_BID_REQUEST_EXAMPLE = """{"teamLeaderId":"leader-02","amount":120}"""
    const val PICK_REQUEST_EXAMPLE = """{"teamLeaderId":"leader-01","playerName":"김민수"}"""

    const val CREATED_ROOM_RESPONSE =
        """
        {
          "resultType":"SUCCESS",
          "success":{
            "code":"ROOM01",
            "status":"WAITING",
            "teamLeaders":[
              {"id":"leader-01","nickname":"호스트","remainingBudget":300}
            ]
          },
          "error":null
        }
        """

    const val JOINED_ROOM_RESPONSE =
        """
        {
          "resultType":"SUCCESS",
          "success":{
            "code":"ROOM01",
            "status":"WAITING",
            "teamLeaders":[
              {"id":"leader-01","nickname":"호스트","remainingBudget":300},
              {"id":"leader-02","nickname":"참가자","remainingBudget":300}
            ]
          },
          "error":null
        }
        """

    const val STARTED_ROOM_RESPONSE =
        """
        {
          "resultType":"SUCCESS",
          "success":{
            "code":"ROOM01",
            "status":"IN_PROGRESS",
            "teamLeaders":[
              {"id":"leader-01","nickname":"호스트","remainingBudget":300},
              {"id":"leader-02","nickname":"참가자","remainingBudget":300}
            ]
          },
          "error":null
        }
        """

    const val ROOM_NOT_FOUND_RESPONSE =
        """
        {
          "resultType":"ERROR",
          "success":null,
          "error":{"status":404,"errorCode":"ROOM_NOT_FOUND","reason":"방을 찾을 수 없습니다","data":null}
        }
        """

    const val TEAM_LEADER_NOT_FOUND_RESPONSE =
        """
        {
          "resultType":"ERROR",
          "success":null,
          "error":{"status":404,"errorCode":"ROOM_TEAM_LEADER_NOT_FOUND","reason":"팀장을 찾을 수 없습니다","data":null}
        }
        """

    const val INTERNAL_ERROR_RESPONSE =
        """
        {
          "resultType":"ERROR",
          "success":null,
          "error":{"status":500,"errorCode":"INTERNAL_ERROR","reason":"예기치 못한 에러가 발생했습니다","data":null}
        }
        """

    const val INVALID_STATE_CREATE_FAILED_RESPONSE =
        """
        {
          "resultType":"ERROR",
          "success":null,
          "error":{"status":409,"errorCode":"INVALID_STATE","reason":"방 코드를 생성할 수 없습니다","data":null}
        }
        """

    const val INVALID_STATE_JOIN_WAITING_REQUIRED_RESPONSE =
        """
        {
          "resultType":"ERROR",
          "success":null,
          "error":{"status":409,"errorCode":"INVALID_STATE","reason":"대기 중인 방에서만 참가할 수 있습니다","data":null}
        }
        """

    const val INVALID_STATE_START_WAITING_REQUIRED_RESPONSE =
        """
        {
          "resultType":"ERROR",
          "success":null,
          "error":{"status":409,"errorCode":"INVALID_STATE","reason":"대기 중인 방에서만 시작할 수 있습니다","data":null}
        }
        """

    const val INVALID_STATE_ROOM_FULL_RESPONSE =
        """
        {
          "resultType":"ERROR",
          "success":null,
          "error":{"status":409,"errorCode":"INVALID_STATE","reason":"방이 가득 찼습니다","data":null}
        }
        """

    const val INVALID_STATE_LEADERS_REQUIRED_RESPONSE =
        """
        {
          "resultType":"ERROR",
          "success":null,
          "error":{"status":409,"errorCode":"INVALID_STATE","reason":"모든 팀장 자리가 채워져야 시작할 수 있습니다","data":null}
        }
        """

    const val INVALID_STATE_PROGRESS_REQUIRED_RESPONSE =
        """
        {
          "resultType":"ERROR",
          "success":null,
          "error":{"status":409,"errorCode":"INVALID_STATE","reason":"진행 중인 방에서만 가능합니다","data":null}
        }
        """

    const val INVALID_STATE_NOT_AUCTION_RESPONSE =
        """{"resultType":"ERROR","success":null,"error":{"status":409,"errorCode":"INVALID_STATE","reason":"경매 모드가 아닙니다","data":null}}"""

    const val INVALID_STATE_NOT_DRAFT_RESPONSE =
        """{"resultType":"ERROR","success":null,"error":{"status":409,"errorCode":"INVALID_STATE","reason":"드래프트 모드가 아닙니다","data":null}}"""

    const val INVALID_STATE_NOT_CURRENT_TURN_RESPONSE =
        """{"resultType":"ERROR","success":null,"error":{"status":409,"errorCode":"INVALID_STATE","reason":"현재 턴이 아닙니다","data":null}}"""

    const val BAD_REQUEST_HIGHER_BID_REQUIRED_RESPONSE =
        """{"resultType":"ERROR","success":null,"error":{"status":400,"errorCode":"BAD_REQUEST","reason":"현재 최고가보다 높아야 합니다","data":null}}"""

    const val BAD_REQUEST_NO_BUDGET_RESPONSE =
        """{"resultType":"ERROR","success":null,"error":{"status":400,"errorCode":"BAD_REQUEST","reason":"예산이 부족합니다","data":null}}"""

    const val BAD_REQUEST_NO_PLAYER_TO_SETTLE_RESPONSE =
        """{"resultType":"ERROR","success":null,"error":{"status":400,"errorCode":"BAD_REQUEST","reason":"경매할 선수가 없습니다","data":null}}"""

    const val BAD_REQUEST_PLAYER_NOT_AVAILABLE_RESPONSE =
        """
        {
          "resultType":"ERROR",
          "success":null,
          "error":{"status":400,"errorCode":"BAD_REQUEST","reason":"선수 '김민수'은(는) 선택할 수 없습니다","data":null}
        }
        """
}
