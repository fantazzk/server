package com.naminhyeok.fantazzk;

public final class OpenApiDocumentation {
    public static final String ROOM_ACTION_TOKEN_HEADER = "X-Room-Action-Token";
    public static final String ROOM_ACTION_TOKEN_SCHEME = "roomActionToken";

    public static final String TEMPLATE_TAG = "Template";
    public static final String ROOM_SESSION_TAG = "Room Session";
    public static final String ROOM_LOBBY_TAG = "Room Lobby";
    public static final String GAME_PLAY_TAG = "Game Play";

    public static final String ROOM_ACTION_TOKEN_DESCRIPTION =
        "방 생성 또는 참가 성공 응답의 `teamLeaderSession.actionToken` 값을 그대로 전달합니다. "
            + "로그인 토큰이 아니라 방/팀장 단위의 액션 권한 토큰입니다.";

    public static final String ROOM_CODE_DESCRIPTION =
        "사용자가 공유받은 방 코드입니다. 로비 조회와 참가, 시작 전 액션의 기준 식별자입니다.";

    public static final String GAME_ID_DESCRIPTION =
        "방이 시작된 뒤 발급되는 게임 ID입니다. 진행 화면의 source of truth는 `/games/{gameId}` 입니다.";

    public static final String OPENAPI_DESCRIPTION = """
        Fantazzk의 외부 웹 연동용 API 문서입니다.

        ## 빠른 사용 흐름
        1. `GET /api/v1/templates` 로 템플릿 목록을 조회합니다.
        2. `POST /api/v1/rooms` 또는 `POST /api/v1/rooms/{code}/join` 으로 방 세션을 시작합니다.
        3. 응답의 `teamLeaderSession.actionToken` 을 FE 세션 스토리지에 저장합니다.
        4. 로비 화면은 `GET /api/v1/rooms/{code}` 를 source of truth 로 사용합니다.
        5. `startedGameId` 가 생기면 진행 화면을 `GET /api/v1/games/{gameId}` 기반으로 전환합니다.
        6. 시작 후 드래프트/경매 액션은 모두 `games` API 로 호출합니다.

        ## 공통 응답 규칙
        - 성공 응답: `resultType=SUCCESS`, 실제 payload 는 `success` 에 들어갑니다.
        - 실패 응답: `resultType=ERROR`, 에러 정보는 `error.code`, `error.message`, `error.data` 에 들어갑니다.
        - validation 실패 시 `error.code` 는 `BAD_REQUEST` 이고, `error.data` 에 필드별 메시지가 들어갑니다.

        ## room 과 game 의 역할
        - `rooms` API 는 로비/대기방 조회와 시작 전 액션을 담당합니다.
        - `games` API 는 시작 후 실시간 진행 상태와 액션을 담당합니다.
        - 즉, 시작 전에는 `room`, 시작 후에는 `game` 이 화면의 source of truth 입니다.

        ## 액션 인증 방식
        - 로그인 토큰 대신 `X-Room-Action-Token` 헤더를 사용합니다.
        - 이 값은 방 생성/참가 응답에서만 내려오며, 이후 mutation API 호출 시 그대로 전달해야 합니다.
        """;

    public static final String TEMPLATE_LIST_SUCCESS_EXAMPLE = """
        {
          "resultType": "SUCCESS",
          "success": [
            {
              "id": "11111111-1111-1111-1111-111111111111",
              "name": "LOL 2인 드래프트",
              "gameType": "LEAGUE_OF_LEGENDS",
              "mode": "DRAFT",
              "teamCount": 2,
              "teamSize": 3,
              "budget": null,
              "pickBanTime": 30,
              "minBidUnit": null,
              "positionLimit": null,
              "draftOrderStrategy": "SNAKE",
              "players": [
                {
                  "name": "선수1",
                  "position": "TOP",
                  "displayOrder": 0
                },
                {
                  "name": "선수2",
                  "position": "JUNGLE",
                  "displayOrder": 1
                }
              ]
            }
          ],
          "error": null
        }
        """;

    public static final String TEMPLATE_DETAIL_SUCCESS_EXAMPLE = """
        {
          "resultType": "SUCCESS",
          "success": {
            "id": "11111111-1111-1111-1111-111111111111",
            "name": "LOL 2인 드래프트",
            "gameType": "LEAGUE_OF_LEGENDS",
            "mode": "DRAFT",
            "teamCount": 2,
            "teamSize": 3,
            "budget": null,
            "pickBanTime": 30,
            "minBidUnit": null,
            "positionLimit": null,
            "draftOrderStrategy": "SNAKE",
            "players": [
              {
                "name": "선수1",
                "position": "TOP",
                "displayOrder": 0
              },
              {
                "name": "선수2",
                "position": "JUNGLE",
                "displayOrder": 1
              }
            ]
          },
          "error": null
        }
        """;

    public static final String ROOM_SESSION_SUCCESS_EXAMPLE = """
        {
          "resultType": "SUCCESS",
          "success": {
            "room": {
              "code": "ROOM01",
              "status": "WAITING",
              "mode": "DRAFT",
              "teamCount": 2,
              "teamSize": 3,
              "budget": null,
              "minBidUnit": null,
              "draftOrderStrategy": "SNAKE",
              "startReadiness": "WAITING_FOR_DRAFT_POSITIONS",
              "startedGameId": null,
              "draftOrderPreview": {
                "slots": [
                  {
                    "draftPosition": 1,
                    "leaderId": "leader-host",
                    "nickname": "호스트"
                  },
                  {
                    "draftPosition": 2,
                    "leaderId": null,
                    "nickname": null
                  }
                ]
              },
              "teamLeaders": [
                {
                  "id": "leader-host",
                  "nickname": "호스트",
                  "draftPosition": 1,
                  "remainingBudget": null
                }
              ],
              "players": [
                {
                  "name": "선수1",
                  "position": "TOP",
                  "displayOrder": 0,
                  "status": "AVAILABLE"
                },
                {
                  "name": "선수2",
                  "position": "JUNGLE",
                  "displayOrder": 1,
                  "status": "AVAILABLE"
                }
              ]
            },
            "teamLeaderSession": {
              "leaderId": "leader-host",
              "role": "HOST",
              "actionToken": "room-action-token"
            }
          },
          "error": null
        }
        """;

    public static final String ROOM_JOIN_SUCCESS_EXAMPLE = """
        {
          "resultType": "SUCCESS",
          "success": {
            "room": {
              "code": "ROOM01",
              "status": "WAITING",
              "mode": "DRAFT",
              "teamCount": 2,
              "teamSize": 3,
              "budget": null,
              "minBidUnit": null,
              "draftOrderStrategy": "SNAKE",
              "startReadiness": "WAITING_FOR_DRAFT_POSITIONS",
              "startedGameId": null,
              "draftOrderPreview": {
                "slots": [
                  {
                    "draftPosition": 1,
                    "leaderId": "leader-host",
                    "nickname": "호스트"
                  },
                  {
                    "draftPosition": 2,
                    "leaderId": null,
                    "nickname": null
                  }
                ]
              },
              "teamLeaders": [
                {
                  "id": "leader-host",
                  "nickname": "호스트",
                  "draftPosition": 1,
                  "remainingBudget": null
                },
                {
                  "id": "leader-guest",
                  "nickname": "게스트",
                  "draftPosition": null,
                  "remainingBudget": null
                }
              ],
              "players": [
                {
                  "name": "선수1",
                  "position": "TOP",
                  "displayOrder": 0,
                  "status": "AVAILABLE"
                },
                {
                  "name": "선수2",
                  "position": "JUNGLE",
                  "displayOrder": 1,
                  "status": "AVAILABLE"
                }
              ]
            },
            "teamLeaderSession": {
              "leaderId": "leader-guest",
              "role": "LEADER",
              "actionToken": "guest-room-action-token"
            }
          },
          "error": null
        }
        """;

    public static final String ROOM_VIEW_SUCCESS_EXAMPLE = """
        {
          "resultType": "SUCCESS",
          "success": {
            "code": "ROOM01",
            "status": "WAITING",
            "mode": "DRAFT",
            "teamCount": 2,
            "teamSize": 3,
            "budget": null,
            "minBidUnit": null,
            "draftOrderStrategy": "SNAKE",
            "startReadiness": "STARTABLE",
            "startedGameId": null,
            "draftOrderPreview": {
              "slots": [
                {
                  "draftPosition": 1,
                  "leaderId": "leader-host",
                  "nickname": "호스트"
                },
                {
                  "draftPosition": 2,
                  "leaderId": "leader-guest",
                  "nickname": "게스트"
                }
              ]
            },
            "teamLeaders": [
              {
                "id": "leader-host",
                "nickname": "호스트",
                "draftPosition": 1,
                "remainingBudget": null
              },
              {
                "id": "leader-guest",
                "nickname": "게스트",
                "draftPosition": 2,
                "remainingBudget": null
              }
            ],
            "players": [
              {
                "name": "선수1",
                "position": "TOP",
                "displayOrder": 0,
                "status": "AVAILABLE"
              },
              {
                "name": "선수2",
                "position": "JUNGLE",
                "displayOrder": 1,
                "status": "AVAILABLE"
              }
            ]
          },
          "error": null
        }
        """;

    public static final String GAME_DRAFT_SUCCESS_EXAMPLE = """
        {
          "resultType": "SUCCESS",
          "success": {
            "id": "00000000-0000-0000-0000-000000000202",
            "roomCode": "ROOM01",
            "mode": "DRAFT",
            "status": "IN_PROGRESS",
            "teamCount": 2,
            "teamSize": 3,
            "budget": null,
            "minBidUnit": null,
            "draftOrderStrategy": "SNAKE",
            "participants": [
              {
                "teamLeaderId": "leader-host",
                "nickname": "호스트",
                "draftPosition": 1,
                "remainingBudget": null
              },
              {
                "teamLeaderId": "leader-guest",
                "nickname": "게스트",
                "draftPosition": 2,
                "remainingBudget": null
              }
            ],
            "players": [
              {
                "name": "선수1",
                "position": "TOP",
                "displayOrder": 0,
                "status": "ASSIGNED"
              },
              {
                "name": "선수2",
                "position": "JUNGLE",
                "displayOrder": 1,
                "status": "AVAILABLE"
              }
            ],
            "members": [
              {
                "teamLeaderId": "leader-host",
                "playerName": "선수1",
                "assignOrder": 0
              }
            ],
            "progress": {
              "currentTurnIndex": 1,
              "currentRound": 1,
              "currentLeaderId": "leader-guest",
              "currentRoundLeaderIds": [
                "leader-host",
                "leader-guest"
              ],
              "currentAuctionRoundEndsAt": null,
              "currentAuctionTarget": null,
              "highestBidAmount": null,
              "leadingLeaderId": null,
              "bidCount": null
            }
          },
          "error": null
        }
        """;

    public static final String GAME_AUCTION_SUCCESS_EXAMPLE = """
        {
          "resultType": "SUCCESS",
          "success": {
            "id": "00000000-0000-0000-0000-000000000201",
            "roomCode": "ROOM01",
            "mode": "AUCTION",
            "status": "IN_PROGRESS",
            "teamCount": 2,
            "teamSize": 3,
            "budget": 300,
            "minBidUnit": 10,
            "draftOrderStrategy": null,
            "participants": [
              {
                "teamLeaderId": "leader-host",
                "nickname": "호스트",
                "draftPosition": null,
                "remainingBudget": 300
              },
              {
                "teamLeaderId": "leader-guest",
                "nickname": "게스트",
                "draftPosition": null,
                "remainingBudget": 200
              }
            ],
            "players": [
              {
                "name": "선수1",
                "position": "TOP",
                "displayOrder": 0,
                "status": "ASSIGNED"
              },
              {
                "name": "선수2",
                "position": "JUNGLE",
                "displayOrder": 1,
                "status": "AVAILABLE"
              }
            ],
            "members": [
              {
                "teamLeaderId": "leader-guest",
                "playerName": "선수1",
                "assignOrder": 0
              }
            ],
            "progress": {
              "currentTurnIndex": null,
              "currentRound": 2,
              "currentLeaderId": null,
              "currentRoundLeaderIds": null,
              "currentAuctionRoundEndsAt": "2026-04-19T12:00:45Z",
              "currentAuctionTarget": {
                "name": "선수2",
                "position": "JUNGLE"
              },
              "highestBidAmount": 150,
              "leadingLeaderId": "leader-guest",
              "bidCount": 2
            }
          },
          "error": null
        }
        """;

    public static final String ROOM_ACTION_TOKEN_REQUIRED_EXAMPLE = """
        {
          "resultType": "ERROR",
          "success": null,
          "error": {
            "code": "ROOM_ACTION_TOKEN_REQUIRED",
            "message": "방 액션 토큰이 필요합니다",
            "data": null
          }
        }
        """;

    public static final String ROOM_CONCURRENT_MODIFICATION_EXAMPLE = """
        {
          "resultType": "ERROR",
          "success": null,
          "error": {
            "code": "ROOM_CONCURRENT_MODIFICATION",
            "message": "방 상태가 동시에 변경되었습니다. 최신 상태를 확인한 뒤 다시 시도해 주세요",
            "data": null
          }
        }
        """;

    public static final String ROOM_NICKNAME_ALREADY_TAKEN_EXAMPLE = """
        {
          "resultType": "ERROR",
          "success": null,
          "error": {
            "code": "ROOM_NICKNAME_ALREADY_TAKEN",
            "message": "이미 사용 중인 닉네임입니다",
            "data": null
          }
        }
        """;

    public static final String BAD_REQUEST_VALIDATION_EXAMPLE = """
        {
          "resultType": "ERROR",
          "success": null,
          "error": {
            "code": "BAD_REQUEST",
            "message": "요청이 올바르지 않습니다",
            "data": {
              "hostNickname": "호스트 이름은 비어 있을 수 없습니다"
            }
          }
        }
        """;

    public static final String BAD_REQUEST_BID_VALIDATION_EXAMPLE = """
        {
          "resultType": "ERROR",
          "success": null,
          "error": {
            "code": "BAD_REQUEST",
            "message": "요청이 올바르지 않습니다",
            "data": {
              "amount": "입찰 금액은 1 이상이어야 합니다"
            }
          }
        }
        """;

    public static final String BAD_REQUEST_JOIN_VALIDATION_EXAMPLE = """
        {
          "resultType": "ERROR",
          "success": null,
          "error": {
            "code": "BAD_REQUEST",
            "message": "요청이 올바르지 않습니다",
            "data": {
              "nickname": "닉네임은 비어 있을 수 없습니다"
            }
          }
        }
        """;

    public static final String ROOM_BID_MIN_UNIT_NOT_MET_EXAMPLE = """
        {
          "resultType": "ERROR",
          "success": null,
          "error": {
            "code": "ROOM_BID_MIN_UNIT_NOT_MET",
            "message": "최소 입찰 증가폭을 만족하는 금액만 입찰할 수 있습니다",
            "data": null
          }
        }
        """;

    public static final String ROOM_PICK_OUT_OF_TURN_EXAMPLE = """
        {
          "resultType": "ERROR",
          "success": null,
          "error": {
            "code": "ROOM_PICK_OUT_OF_TURN",
            "message": "현재 턴인 팀장만 픽할 수 있습니다",
            "data": null
          }
        }
        """;

    private OpenApiDocumentation() {
    }
}
