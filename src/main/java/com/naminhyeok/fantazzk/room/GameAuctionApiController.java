package com.naminhyeok.fantazzk.room;

import com.naminhyeok.fantazzk.ApiResponse;
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
    private final PlaceBid placeBid;
    private final SettleAuction settleAuction;
    private final GetGame getGame;

    @PostMapping("/{gameId}/bids")
    ApiResponse<GameResponse> placeBid(
        @PathVariable UUID gameId,
        @RequestHeader(value = "X-Room-Action-Token", required = false) String actionToken,
        @Valid @RequestBody PlaceBidRequest request
    ) {
        placeBid.place(gameId, actionToken, request.amount());
        return ApiResponse.success(GameResponse.from(getGame.get(gameId)));
    }

    @PostMapping("/{gameId}/auction/progress")
    ApiResponse<GameResponse> progressAuction(@PathVariable UUID gameId) {
        return ApiResponse.success(GameResponse.from(settleAuction.settleIfDue(gameId)));
    }
}
