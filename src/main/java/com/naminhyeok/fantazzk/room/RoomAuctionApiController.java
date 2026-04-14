package com.naminhyeok.fantazzk.room;

import com.naminhyeok.fantazzk.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/rooms")
@RequiredArgsConstructor
class RoomAuctionApiController {
    private final StartRoom startRoom;
    private final PlaceBid placeBid;
    private final SettleAuction settleAuction;

    @PostMapping("/{code}/start")
    ApiResponse<RoomResponse> start(
        @PathVariable String code,
        @RequestHeader(value = "X-Room-Action-Token", required = false) String actionToken
    ) {
        return ApiResponse.success(RoomResponse.from(startRoom.start(code, actionToken)));
    }

    @PostMapping("/{code}/bids")
    ApiResponse<RoomResponse> placeBid(
        @PathVariable String code,
        @RequestHeader(value = "X-Room-Action-Token", required = false) String actionToken,
        @Valid @RequestBody PlaceBidRequest request
    ) {
        return ApiResponse.success(RoomResponse.from(placeBid.place(code, actionToken, request.amount())));
    }

    @PostMapping("/{code}/auction/progress")
    ApiResponse<RoomResponse> progressAuction(@PathVariable String code) {
        return ApiResponse.success(RoomResponse.from(settleAuction.settleIfDue(code)));
    }
}
