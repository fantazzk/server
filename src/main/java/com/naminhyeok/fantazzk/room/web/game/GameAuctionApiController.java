package com.naminhyeok.fantazzk.room.web.game;

import com.naminhyeok.fantazzk.ApiResponse;
import com.naminhyeok.fantazzk.room.GameAuctionApi;
import com.naminhyeok.fantazzk.room.GameView;
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
class GameAuctionApiController {
    private final GameAuctionApi gameAuctionApi;

    @PostMapping("/{gameId}/bids")
    ApiResponse<GameView> placeBid(
        @PathVariable UUID gameId,
        @RequestHeader(value = "X-Room-Action-Token", required = false) String actionToken,
        @Valid @RequestBody PlaceBidRequest request
    ) {
        return ApiResponse.success(gameAuctionApi.placeBid(gameId, actionToken, request.amount()));
    }

    @PostMapping("/{gameId}/auction/progress")
    ApiResponse<GameView> progressAuction(@PathVariable UUID gameId) {
        return ApiResponse.success(gameAuctionApi.settleIfDue(gameId));
    }
}
