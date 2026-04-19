package com.naminhyeok.fantazzk.room;

import com.naminhyeok.fantazzk.ApiResponse;
import com.naminhyeok.fantazzk.OpenApiDocumentation;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/games")
@RequiredArgsConstructor
@Tag(name = OpenApiDocumentation.GAME_PLAY_TAG)
class GameDraftApiController {
    private final PickDraft pickDraft;
    private final GetGame getGame;

    @PostMapping("/{gameId}/draft-picks")
    @Operation(
        summary = "드래프트 픽",
        description = "현재 턴인 팀장이 선수를 선택합니다. 성공 시 최신 `GameResponse` 를 다시 반환하므로 FE는 응답 그대로 화면을 갱신하면 됩니다.",
        security = @SecurityRequirement(name = OpenApiDocumentation.ROOM_ACTION_TOKEN_SCHEME)
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "픽 성공",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(value = OpenApiDocumentation.GAME_DRAFT_SUCCESS_EXAMPLE)
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
            responseCode = "409",
            description = "현재 턴이 아니거나 이미 선택된 선수를 픽하려고 시도함",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(value = OpenApiDocumentation.ROOM_PICK_OUT_OF_TURN_EXAMPLE)
            )
        )
    })
    ApiResponse<GameResponse> pickDraft(
        @Parameter(description = OpenApiDocumentation.GAME_ID_DESCRIPTION, example = "00000000-0000-0000-0000-000000000202")
        @PathVariable UUID gameId,
        @Parameter(description = OpenApiDocumentation.ROOM_ACTION_TOKEN_DESCRIPTION)
        @RequestHeader(value = OpenApiDocumentation.ROOM_ACTION_TOKEN_HEADER, required = false) String actionToken,
        @Valid @RequestBody PickDraftRequest request
    ) {
        pickDraft.pick(gameId, actionToken, request.playerName());
        return ApiResponse.success(GameResponse.from(getGame.get(gameId)));
    }
}
