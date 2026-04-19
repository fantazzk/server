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
class GameAuctionApiController {
    private final PlaceBid placeBid;
    private final SettleAuction settleAuction;
    private final GetGame getGame;

    @PostMapping("/{gameId}/bids")
    @Operation(
        summary = "경매 입찰",
        description = "현재 경매 대상 선수에게 입찰합니다. 성공 시 입찰 결과와 최신 경매 진행 상태를 반환하며, 입찰 시점 기준으로 라운드 마감 시간이 다시 연장됩니다.",
        security = @SecurityRequirement(name = OpenApiDocumentation.ROOM_ACTION_TOKEN_SCHEME)
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "입찰 성공",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(value = OpenApiDocumentation.GAME_AUCTION_SUCCESS_EXAMPLE)
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
    ApiResponse<BidPlacementResponse> placeBid(
        @Parameter(description = OpenApiDocumentation.GAME_ID_DESCRIPTION, example = "00000000-0000-0000-0000-000000000201")
        @PathVariable UUID gameId,
        @Parameter(description = OpenApiDocumentation.ROOM_ACTION_TOKEN_DESCRIPTION)
        @RequestHeader(value = OpenApiDocumentation.ROOM_ACTION_TOKEN_HEADER, required = false) String actionToken,
        @Valid @RequestBody PlaceBidRequest request
    ) {
        AuctionBid bid = placeBid.place(gameId, actionToken, request.amount());
        return ApiResponse.success(BidPlacementResponse.from(bid, getGame.get(gameId)));
    }

    @PostMapping("/{gameId}/auction/progress")
    @Operation(
        summary = "경매 진행 상태 갱신",
        description = "현재 시각 기준으로 경매 라운드가 마감되었는지 확인하고, 필요하면 정산 후 최신 경매 진행 상태와 로스터를 반환합니다. 실시간 연동이 없을 때 FE 폴링 fallback 으로 사용할 수 있습니다."
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
        responseCode = "200",
        description = "최신 경매 상태 반환",
        content = @Content(
            mediaType = "application/json",
            examples = @ExampleObject(value = OpenApiDocumentation.GAME_AUCTION_SUCCESS_EXAMPLE)
        )
    )
    ApiResponse<AuctionProgressUpdateResponse> progressAuction(@PathVariable UUID gameId) {
        return ApiResponse.success(AuctionProgressUpdateResponse.from(settleAuction.settleIfDue(gameId)));
    }
}
