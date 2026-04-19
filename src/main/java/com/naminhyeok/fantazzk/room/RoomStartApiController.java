package com.naminhyeok.fantazzk.room;

import com.naminhyeok.fantazzk.ApiResponse;
import com.naminhyeok.fantazzk.OpenApiDocumentation;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/rooms")
@RequiredArgsConstructor
@Tag(name = OpenApiDocumentation.ROOM_LOBBY_TAG)
class RoomStartApiController {
    private final StartRoom startRoom;

    @PostMapping("/{code}/start")
    @Operation(
        summary = "방 시작",
        description = "호스트가 로비를 실제 게임으로 시작합니다. 성공 시 응답으로 `GameResponse` 를 반환하며, FE는 이후 `/games/{gameId}` 를 기준으로 진행 화면을 유지해야 합니다.",
        security = @SecurityRequirement(name = OpenApiDocumentation.ROOM_ACTION_TOKEN_SCHEME)
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "방 시작 성공",
            content = @Content(
                mediaType = "application/json",
                examples = {
                    @ExampleObject(name = "draft", value = OpenApiDocumentation.GAME_DRAFT_SUCCESS_EXAMPLE),
                    @ExampleObject(name = "auction", value = OpenApiDocumentation.GAME_AUCTION_SUCCESS_EXAMPLE)
                }
            )
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "401",
            description = "액션 토큰이 없거나 유효하지 않음",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(value = OpenApiDocumentation.ROOM_ACTION_TOKEN_REQUIRED_EXAMPLE)
            )
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "403",
            description = "호스트가 아닌 팀장이 시작을 시도함"
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "409",
            description = "팀장 수 또는 드래프트 자리 준비가 완료되지 않았거나 동시 수정 충돌이 발생함",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(value = OpenApiDocumentation.ROOM_CONCURRENT_MODIFICATION_EXAMPLE)
            )
        )
    })
    ApiResponse<GameResponse> start(
        @Parameter(description = OpenApiDocumentation.ROOM_CODE_DESCRIPTION, example = "ROOM01")
        @PathVariable String code,
        @Parameter(description = OpenApiDocumentation.ROOM_ACTION_TOKEN_DESCRIPTION)
        @RequestHeader(value = OpenApiDocumentation.ROOM_ACTION_TOKEN_HEADER, required = false) String actionToken
    ) {
        return ApiResponse.success(GameResponse.from(startRoom.start(code, actionToken)));
    }
}
