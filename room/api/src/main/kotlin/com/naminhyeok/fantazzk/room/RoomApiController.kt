package com.naminhyeok.fantazzk.room

import com.naminhyeok.fantazzk.room.dto.ApiResponse
import com.naminhyeok.fantazzk.room.dto.CreateRoomRequest
import com.naminhyeok.fantazzk.room.dto.JoinRoomRequest
import com.naminhyeok.fantazzk.room.dto.PickRequest
import com.naminhyeok.fantazzk.room.dto.PlaceBidRequest
import com.naminhyeok.fantazzk.room.dto.RoomResponse
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.ExampleObject
import io.swagger.v3.oas.annotations.responses.ApiResponse as SwaggerApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.tags.Tag
import io.swagger.v3.oas.annotations.parameters.RequestBody as SwaggerRequestBody
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController

@Tag(name = "Room", description = RoomOpenApiDocs.TAG_DESCRIPTION)
@RestController
@RequestMapping("/api/v1/rooms")
class RoomApiController(
    private val roomCreateService: RoomCreateService,
    private val roomLookupService: RoomLookupService,
    private val roomJoinService: RoomJoinService,
    private val roomStartService: RoomStartService,
    private val auctionService: AuctionService,
    private val draftService: DraftService,
) {
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "방 생성", operationId = "createRoom", description = RoomOpenApiDocs.CREATE_DESCRIPTION)
    @ApiResponses(
        value = [
            SwaggerApiResponse(
                responseCode = "201",
                description = "방 생성 성공",
                content = [
                    Content(
                        mediaType = MediaType.APPLICATION_JSON_VALUE,
                        examples = [ExampleObject(name = "createdRoom", value = RoomOpenApiDocs.CREATED_ROOM_RESPONSE)],
                    ),
                ],
            ),
            SwaggerApiResponse(
                responseCode = "409",
                description = "방 코드를 생성할 수 없습니다",
                content = [
                    Content(
                        mediaType = MediaType.APPLICATION_JSON_VALUE,
                        examples = [ExampleObject(name = "codeGenerationFailed", value = RoomOpenApiDocs.INVALID_STATE_CREATE_FAILED_RESPONSE)],
                    ),
                ],
            ),
            SwaggerApiResponse(
                responseCode = "500",
                description = "템플릿 조회 실패 등 예기치 못한 내부 오류가 발생했습니다",
                content = [
                    Content(
                        mediaType = MediaType.APPLICATION_JSON_VALUE,
                        examples = [ExampleObject(name = "internalError", value = RoomOpenApiDocs.INTERNAL_ERROR_RESPONSE)],
                    ),
                ],
            ),
        ],
    )
    fun create(
        @SwaggerRequestBody(
            required = true,
            description = "생성에 사용할 템플릿 ID와 호스트 닉네임입니다.",
            content = [
                Content(
                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                    examples = [ExampleObject(name = "createRoom", value = RoomOpenApiDocs.CREATE_ROOM_REQUEST_EXAMPLE)],
                ),
            ],
        )
        @RequestBody request: CreateRoomRequest,
    ): ApiResponse<RoomResponse> {
        val room = roomCreateService.create(request.templateId, request.hostNickname)
        val leaders = roomLookupService.getTeamLeaders(room.roomId)
        return ApiResponse.success(RoomResponse.from(room, leaders))
    }

    @GetMapping("/{code}")
    @Operation(summary = "방 조회", operationId = "getRoom", description = RoomOpenApiDocs.GET_DESCRIPTION)
    @ApiResponses(
        value = [
            SwaggerApiResponse(
                responseCode = "200",
                description = "방 조회 성공",
                content = [
                    Content(
                        mediaType = MediaType.APPLICATION_JSON_VALUE,
                        examples = [ExampleObject(name = "roomDetail", value = RoomOpenApiDocs.CREATED_ROOM_RESPONSE)],
                    ),
                ],
            ),
            SwaggerApiResponse(
                responseCode = "404",
                description = "존재하지 않는 방입니다",
                content = [
                    Content(
                        mediaType = MediaType.APPLICATION_JSON_VALUE,
                        examples = [ExampleObject(name = "roomNotFound", value = RoomOpenApiDocs.ROOM_NOT_FOUND_RESPONSE)],
                    ),
                ],
            ),
        ],
    )
    fun getByCode(
        @Parameter(description = RoomOpenApiDocs.ROOM_CODE_PARAMETER, example = "ROOM01")
        @PathVariable code: String,
    ): ApiResponse<RoomResponse> {
        val room = roomLookupService.get(code)
        val leaders = roomLookupService.getTeamLeaders(room.roomId)
        return ApiResponse.success(RoomResponse.from(room, leaders))
    }

    @PostMapping("/{code}/join")
    @Operation(summary = "방 참가", operationId = "joinRoom", description = RoomOpenApiDocs.JOIN_DESCRIPTION)
    @ApiResponses(
        value = [
            SwaggerApiResponse(
                responseCode = "200",
                description = "방 참가 성공",
                content = [
                    Content(
                        mediaType = MediaType.APPLICATION_JSON_VALUE,
                        examples = [ExampleObject(name = "joinedRoom", value = RoomOpenApiDocs.JOINED_ROOM_RESPONSE)],
                    ),
                ],
            ),
            SwaggerApiResponse(
                responseCode = "404",
                description = "존재하지 않는 방입니다",
                content = [
                    Content(
                        mediaType = MediaType.APPLICATION_JSON_VALUE,
                        examples = [ExampleObject(name = "roomNotFound", value = RoomOpenApiDocs.ROOM_NOT_FOUND_RESPONSE)],
                    ),
                ],
            ),
            SwaggerApiResponse(
                responseCode = "409",
                description = "현재 상태에서는 방에 참가할 수 없습니다",
                content = [
                    Content(
                        mediaType = MediaType.APPLICATION_JSON_VALUE,
                        examples = [
                            ExampleObject(name = "roomIsFull", value = RoomOpenApiDocs.INVALID_STATE_ROOM_FULL_RESPONSE),
                            ExampleObject(name = "roomIsNotWaiting", value = RoomOpenApiDocs.INVALID_STATE_JOIN_WAITING_REQUIRED_RESPONSE),
                        ],
                    ),
                ],
            ),
        ],
    )
    fun join(
        @Parameter(description = RoomOpenApiDocs.ROOM_CODE_PARAMETER, example = "ROOM01")
        @PathVariable code: String,
        @SwaggerRequestBody(
            required = true,
            description = "참가할 팀장의 닉네임입니다.",
            content = [
                Content(
                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                    examples = [ExampleObject(name = "joinRoom", value = RoomOpenApiDocs.JOIN_ROOM_REQUEST_EXAMPLE)],
                ),
            ],
        )
        @RequestBody request: JoinRoomRequest,
    ): ApiResponse<RoomResponse> {
        roomJoinService.join(code, request.nickname)
        val room = roomLookupService.get(code)
        val leaders = roomLookupService.getTeamLeaders(room.roomId)
        return ApiResponse.success(RoomResponse.from(room, leaders))
    }

    @PostMapping("/{code}/start")
    @Operation(summary = "방 시작", operationId = "startRoom", description = RoomOpenApiDocs.START_DESCRIPTION)
    @ApiResponses(
        value = [
            SwaggerApiResponse(
                responseCode = "200",
                description = "방 시작 성공",
                content = [
                    Content(
                        mediaType = MediaType.APPLICATION_JSON_VALUE,
                        examples = [ExampleObject(name = "startedRoom", value = RoomOpenApiDocs.STARTED_ROOM_RESPONSE)],
                    ),
                ],
            ),
            SwaggerApiResponse(
                responseCode = "404",
                description = "존재하지 않는 방입니다",
                content = [
                    Content(
                        mediaType = MediaType.APPLICATION_JSON_VALUE,
                        examples = [ExampleObject(name = "roomNotFound", value = RoomOpenApiDocs.ROOM_NOT_FOUND_RESPONSE)],
                    ),
                ],
            ),
            SwaggerApiResponse(
                responseCode = "409",
                description = "현재 상태에서는 방을 시작할 수 없습니다",
                content = [
                    Content(
                        mediaType = MediaType.APPLICATION_JSON_VALUE,
                        examples = [
                            ExampleObject(name = "invalidState", value = RoomOpenApiDocs.INVALID_STATE_START_WAITING_REQUIRED_RESPONSE),
                            ExampleObject(name = "leadersRequired", value = RoomOpenApiDocs.INVALID_STATE_LEADERS_REQUIRED_RESPONSE),
                        ],
                    ),
                ],
            ),
        ],
    )
    fun start(
        @Parameter(description = RoomOpenApiDocs.ROOM_CODE_PARAMETER, example = "ROOM01")
        @PathVariable code: String,
    ): ApiResponse<RoomResponse> {
        roomStartService.start(code)
        val room = roomLookupService.get(code)
        val leaders = roomLookupService.getTeamLeaders(room.roomId)
        return ApiResponse.success(RoomResponse.from(room, leaders))
    }

    @PostMapping("/{code}/bid")
    @Operation(summary = "입찰", operationId = "placeBid", description = RoomOpenApiDocs.BID_DESCRIPTION)
    @ApiResponses(
        value = [
            SwaggerApiResponse(
                responseCode = "200",
                description = "입찰 성공",
                content = [
                    Content(
                        mediaType = MediaType.APPLICATION_JSON_VALUE,
                        examples = [ExampleObject(name = "startedRoom", value = RoomOpenApiDocs.STARTED_ROOM_RESPONSE)],
                    ),
                ],
            ),
            SwaggerApiResponse(
                responseCode = "400",
                description = "입찰 조건을 만족하지 않습니다",
                content = [
                    Content(
                        mediaType = MediaType.APPLICATION_JSON_VALUE,
                        examples = [
                            ExampleObject(name = "higherBidRequired", value = RoomOpenApiDocs.BAD_REQUEST_HIGHER_BID_REQUIRED_RESPONSE),
                            ExampleObject(name = "insufficientBudget", value = RoomOpenApiDocs.BAD_REQUEST_NO_BUDGET_RESPONSE),
                        ],
                    ),
                ],
            ),
            SwaggerApiResponse(
                responseCode = "404",
                description = "방 또는 팀장을 찾을 수 없습니다",
                content = [
                    Content(
                        mediaType = MediaType.APPLICATION_JSON_VALUE,
                        examples = [
                            ExampleObject(name = "roomNotFound", value = RoomOpenApiDocs.ROOM_NOT_FOUND_RESPONSE),
                            ExampleObject(name = "teamLeaderNotFound", value = RoomOpenApiDocs.TEAM_LEADER_NOT_FOUND_RESPONSE),
                        ],
                    ),
                ],
            ),
            SwaggerApiResponse(
                responseCode = "409",
                description = "현재 상태에서는 입찰할 수 없습니다",
                content = [
                    Content(
                        mediaType = MediaType.APPLICATION_JSON_VALUE,
                        examples = [
                            ExampleObject(name = "progressRequired", value = RoomOpenApiDocs.INVALID_STATE_PROGRESS_REQUIRED_RESPONSE),
                            ExampleObject(name = "notAuction", value = RoomOpenApiDocs.INVALID_STATE_NOT_AUCTION_RESPONSE),
                        ],
                    ),
                ],
            ),
        ],
    )
    fun placeBid(
        @Parameter(description = RoomOpenApiDocs.ROOM_CODE_PARAMETER, example = "ROOM01")
        @PathVariable code: String,
        @SwaggerRequestBody(
            required = true,
            description = "입찰을 수행할 팀장 ID와 입찰 금액입니다.",
            content = [
                Content(
                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                    examples = [ExampleObject(name = "placeBid", value = RoomOpenApiDocs.PLACE_BID_REQUEST_EXAMPLE)],
                ),
            ],
        )
        @RequestBody request: PlaceBidRequest,
    ): ApiResponse<RoomResponse> {
        auctionService.placeBid(code, request.teamLeaderId, request.amount)
        val room = roomLookupService.get(code)
        val leaders = roomLookupService.getTeamLeaders(room.roomId)
        return ApiResponse.success(RoomResponse.from(room, leaders))
    }

    @PostMapping("/{code}/settle")
    @Operation(summary = "경매 정산", operationId = "settleAuction", description = RoomOpenApiDocs.SETTLE_DESCRIPTION)
    @ApiResponses(
        value = [
            SwaggerApiResponse(
                responseCode = "200",
                description = "경매 정산 성공",
                content = [
                    Content(
                        mediaType = MediaType.APPLICATION_JSON_VALUE,
                        examples = [ExampleObject(name = "startedRoom", value = RoomOpenApiDocs.STARTED_ROOM_RESPONSE)],
                    ),
                ],
            ),
            SwaggerApiResponse(
                responseCode = "400",
                description = "정산할 선수가 없습니다",
                content = [
                    Content(
                        mediaType = MediaType.APPLICATION_JSON_VALUE,
                        examples = [ExampleObject(name = "noPlayerToSettle", value = RoomOpenApiDocs.BAD_REQUEST_NO_PLAYER_TO_SETTLE_RESPONSE)],
                    ),
                ],
            ),
            SwaggerApiResponse(
                responseCode = "404",
                description = "존재하지 않는 방입니다",
                content = [
                    Content(
                        mediaType = MediaType.APPLICATION_JSON_VALUE,
                        examples = [ExampleObject(name = "roomNotFound", value = RoomOpenApiDocs.ROOM_NOT_FOUND_RESPONSE)],
                    ),
                ],
            ),
            SwaggerApiResponse(
                responseCode = "409",
                description = "현재 상태에서는 경매 정산을 진행할 수 없습니다",
                content = [
                    Content(
                        mediaType = MediaType.APPLICATION_JSON_VALUE,
                        examples = [
                            ExampleObject(name = "progressRequired", value = RoomOpenApiDocs.INVALID_STATE_PROGRESS_REQUIRED_RESPONSE),
                            ExampleObject(name = "notAuction", value = RoomOpenApiDocs.INVALID_STATE_NOT_AUCTION_RESPONSE),
                        ],
                    ),
                ],
            ),
        ],
    )
    fun settle(
        @Parameter(description = RoomOpenApiDocs.ROOM_CODE_PARAMETER, example = "ROOM01")
        @PathVariable code: String,
    ): ApiResponse<RoomResponse> {
        auctionService.settle(code)
        val room = roomLookupService.get(code)
        val leaders = roomLookupService.getTeamLeaders(room.roomId)
        return ApiResponse.success(RoomResponse.from(room, leaders))
    }

    @PostMapping("/{code}/pick")
    @Operation(summary = "드래프트 픽", operationId = "pick", description = RoomOpenApiDocs.PICK_DESCRIPTION)
    @ApiResponses(
        value = [
            SwaggerApiResponse(
                responseCode = "200",
                description = "드래프트 픽 성공",
                content = [
                    Content(
                        mediaType = MediaType.APPLICATION_JSON_VALUE,
                        examples = [ExampleObject(name = "startedRoom", value = RoomOpenApiDocs.STARTED_ROOM_RESPONSE)],
                    ),
                ],
            ),
            SwaggerApiResponse(
                responseCode = "400",
                description = "선수를 현재 시점에 선택할 수 없습니다",
                content = [
                    Content(
                        mediaType = MediaType.APPLICATION_JSON_VALUE,
                        examples = [ExampleObject(name = "playerNotAvailable", value = RoomOpenApiDocs.BAD_REQUEST_PLAYER_NOT_AVAILABLE_RESPONSE)],
                    ),
                ],
            ),
            SwaggerApiResponse(
                responseCode = "404",
                description = "방 또는 팀장을 찾을 수 없습니다",
                content = [
                    Content(
                        mediaType = MediaType.APPLICATION_JSON_VALUE,
                        examples = [
                            ExampleObject(name = "roomNotFound", value = RoomOpenApiDocs.ROOM_NOT_FOUND_RESPONSE),
                            ExampleObject(name = "teamLeaderNotFound", value = RoomOpenApiDocs.TEAM_LEADER_NOT_FOUND_RESPONSE),
                        ],
                    ),
                ],
            ),
            SwaggerApiResponse(
                responseCode = "409",
                description = "현재 상태에서는 드래프트 픽을 진행할 수 없습니다",
                content = [
                    Content(
                        mediaType = MediaType.APPLICATION_JSON_VALUE,
                        examples = [
                            ExampleObject(name = "progressRequired", value = RoomOpenApiDocs.INVALID_STATE_PROGRESS_REQUIRED_RESPONSE),
                            ExampleObject(name = "notDraft", value = RoomOpenApiDocs.INVALID_STATE_NOT_DRAFT_RESPONSE),
                            ExampleObject(name = "notCurrentTurn", value = RoomOpenApiDocs.INVALID_STATE_NOT_CURRENT_TURN_RESPONSE),
                        ],
                    ),
                ],
            ),
        ],
    )
    fun pick(
        @Parameter(description = RoomOpenApiDocs.ROOM_CODE_PARAMETER, example = "ROOM01")
        @PathVariable code: String,
        @SwaggerRequestBody(
            required = true,
            description = "픽을 수행할 팀장 ID와 선택할 선수 이름입니다.",
            content = [
                Content(
                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                    examples = [ExampleObject(name = "pick", value = RoomOpenApiDocs.PICK_REQUEST_EXAMPLE)],
                ),
            ],
        )
        @RequestBody request: PickRequest,
    ): ApiResponse<RoomResponse> {
        draftService.pick(code, request.teamLeaderId, request.playerName)
        val room = roomLookupService.get(code)
        val leaders = roomLookupService.getTeamLeaders(room.roomId)
        return ApiResponse.success(RoomResponse.from(room, leaders))
    }
}
