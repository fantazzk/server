package com.naminhyeok.fantazzk.template.api

internal object TemplateOpenApiDocs {
    const val TAG_DESCRIPTION = "팀 빌딩에 사용할 템플릿을 생성하고 조회하는 API입니다."

    const val CREATE_DESCRIPTION =
        "새 템플릿을 생성합니다.\n" +
            "- mode 가 AUCTION 이면 budget 값을 사용합니다.\n" +
            "- mode 가 DRAFT 이면 draftOrderStrategy 값을 사용합니다.\n" +
            "- 현재 구현에서는 teamCount 와 teamSize 가 0보다 커야 합니다.\n" +
            "- budget 이 있으면 0보다 커야 합니다.\n" +
            "- playerNames 는 정확히 teamCount * (teamSize - 1) 명이어야 합니다.\n" +
            "- playerNames 의 순서가 템플릿 선수의 displayOrder 로 저장됩니다."

    const val GET_DESCRIPTION =
        "템플릿 ID로 상세 정보를 조회합니다.\n" +
            "- success.players 에 템플릿에 속한 선수 목록이 포함됩니다.\n" +
            "- players.displayOrder 로 저장 순서를 확인할 수 있습니다."

    const val LIST_DESCRIPTION =
        "등록된 템플릿 목록을 조회합니다.\n" +
            "- 목록 조회에서는 기본 메타데이터를 중심으로 반환합니다.\n" +
            "- success 는 TemplateResponse 배열입니다."

    const val TEMPLATE_ID_PARAMETER = "조회할 템플릿의 ID 입니다."

    const val CREATE_AUCTION_TEMPLATE_REQUEST_EXAMPLE =
        """
        {
          "name":"주말 풋살 경매전",
          "mode":"AUCTION",
          "teamCount":2,
          "teamSize":2,
          "budget":300,
          "draftOrderStrategy":null,
          "playerNames":["김민수","이준호"]
        }
        """

    const val CREATE_DRAFT_TEMPLATE_REQUEST_EXAMPLE =
        """
        {
          "name":"사내 리그 드래프트전",
          "mode":"DRAFT",
          "teamCount":2,
          "teamSize":2,
          "budget":null,
          "draftOrderStrategy":"SNAKE",
          "playerNames":["김민수","이준호"]
        }
        """

    const val CREATED_TEMPLATE_RESPONSE =
        """
        {
          "resultType":"SUCCESS",
          "success":{
            "id":1,
            "name":"주말 풋살 경매전",
            "mode":"AUCTION",
            "teamCount":2,
            "teamSize":2,
            "budget":300,
            "draftOrderStrategy":null,
            "players":null
          },
          "error":null
        }
        """

    const val TEMPLATE_DETAIL_RESPONSE =
        """
        {
          "resultType":"SUCCESS",
          "success":{
            "id":1,
            "name":"사내 리그 드래프트전",
            "mode":"DRAFT",
            "teamCount":2,
            "teamSize":2,
            "budget":null,
            "draftOrderStrategy":"SNAKE",
            "players":[
              {"name":"김민수","displayOrder":0},
              {"name":"이준호","displayOrder":1}
            ]
          },
          "error":null
        }
        """

    const val TEMPLATE_LIST_RESPONSE =
        """
        {
          "resultType":"SUCCESS",
          "success":[
            {"id":1,"name":"주말 풋살 경매전","mode":"AUCTION","teamCount":2,"teamSize":2,"budget":300,"draftOrderStrategy":null,"players":null},
            {"id":2,"name":"사내 리그 드래프트전","mode":"DRAFT","teamCount":2,"teamSize":2,"budget":null,"draftOrderStrategy":"SNAKE","players":null}
          ],
          "error":null
        }
        """

    const val TEMPLATE_NOT_FOUND_RESPONSE =
        """
        {
          "resultType":"ERROR",
          "success":null,
          "error":{"status":404,"errorCode":"TEMPLATE_NOT_FOUND","reason":"템플릿을 찾을 수 없습니다","data":null}
        }
        """

    const val TEMPLATE_TEAM_COUNT_BAD_REQUEST_RESPONSE =
        """
        {
          "resultType":"ERROR",
          "success":null,
          "error":{"status":400,"errorCode":"BAD_REQUEST","reason":"팀 수는 0보다 커야 합니다","data":null}
        }
        """

    const val TEMPLATE_TEAM_SIZE_BAD_REQUEST_RESPONSE =
        """
        {
          "resultType":"ERROR",
          "success":null,
          "error":{"status":400,"errorCode":"BAD_REQUEST","reason":"팀 크기는 0보다 커야 합니다","data":null}
        }
        """

    const val TEMPLATE_BUDGET_BAD_REQUEST_RESPONSE =
        """
        {
          "resultType":"ERROR",
          "success":null,
          "error":{"status":400,"errorCode":"BAD_REQUEST","reason":"예산은 0보다 커야 합니다","data":null}
        }
        """

    const val TEMPLATE_PLAYER_COUNT_BAD_REQUEST_RESPONSE =
        """
        {
          "resultType":"ERROR",
          "success":null,
          "error":{"status":400,"errorCode":"BAD_REQUEST","reason":"선수 수는 정확히 2명이어야 합니다","data":null}
        }
        """
}
