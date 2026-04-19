package com.naminhyeok.fantazzk.room;

import com.naminhyeok.fantazzk.ApiResponse;
import com.naminhyeok.fantazzk.OpenApiDocumentation;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/games")
@RequiredArgsConstructor
@Tag(name = OpenApiDocumentation.GAME_PLAY_TAG)
class GameQueryApiController {
    private final GetGame getGame;

    @GetMapping("/{gameId}")
    @Operation(
        summary = "게임 진행 상태 조회",
        description = "방 시작 후 진행 화면의 source of truth 입니다. 드래프트/경매 상태, 참가자, 멤버, 현재 진행 정보를 모두 이 API 기준으로 렌더링합니다."
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "게임 상태 조회 성공",
            content = @Content(
                mediaType = "application/json",
                examples = {
                    @ExampleObject(name = "draft", value = OpenApiDocumentation.GAME_DRAFT_SUCCESS_EXAMPLE),
                    @ExampleObject(name = "auction", value = OpenApiDocumentation.GAME_AUCTION_SUCCESS_EXAMPLE)
                }
            )
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "404",
            description = "게임을 찾을 수 없음"
        )
    })
    ApiResponse<GameResponse> get(
        @Parameter(description = OpenApiDocumentation.GAME_ID_DESCRIPTION, example = "00000000-0000-0000-0000-000000000201")
        @PathVariable UUID gameId
    ) {
        return ApiResponse.success(GameResponse.from(getGame.get(gameId)));
    }
}
