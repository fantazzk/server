package com.naminhyeok.fantazzk.room.web;

import com.naminhyeok.fantazzk.ApiResponse;
import com.naminhyeok.fantazzk.OpenApiDocumentation;
import com.naminhyeok.fantazzk.room.application.PlaceBid;
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
public class GameAuctionApiController {
    private final PlaceBid placeBid;

    @PostMapping("/{gameId}/bids")
    @Operation(
        summary = "경매 입찰",
        description = "현재 경매 대상 선수에게 입찰 요청을 전달합니다. 성공 시에는 수락 여부만 빈 성공 응답으로 확인하며, 화면 갱신과 최신 경매 진행 상태는 realtime 이벤트나 GET /games/{gameId}로 확인합니다.",
        security = @SecurityRequirement(name = OpenApiDocumentation.ROOM_ACTION_TOKEN_SCHEME)
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "입찰 성공",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(value = OpenApiDocumentation.EMPTY_SUCCESS_EXAMPLE)
            )
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "400",
            description = "0 이하 금액, 예산 초과 등 잘못된 입찰",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(value = OpenApiDocumentation.BAD_REQUEST_BID_VALIDATION_EXAMPLE)
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
            description = "최고가/최소 증가폭/포지션 제한/동시 수정 충돌 등으로 입찰이 거부됨",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(value = OpenApiDocumentation.ROOM_BID_MIN_UNIT_NOT_MET_EXAMPLE)
            )
        )
    })
    ApiResponse<Void> placeBid(
        @Parameter(description = OpenApiDocumentation.GAME_ID_DESCRIPTION, example = "00000000-0000-0000-0000-000000000201")
        @PathVariable UUID gameId,
        @Parameter(description = OpenApiDocumentation.ROOM_ACTION_TOKEN_DESCRIPTION)
        @RequestHeader(value = OpenApiDocumentation.ROOM_ACTION_TOKEN_HEADER, required = false) String actionToken,
        @Valid @RequestBody PlaceBidRequest request
    ) {
        placeBid.place(gameId, actionToken, request.amount());
        return ApiResponse.success();
    }
}
