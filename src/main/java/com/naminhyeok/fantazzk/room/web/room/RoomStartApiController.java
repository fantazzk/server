package com.naminhyeok.fantazzk.room.web.room;

import com.naminhyeok.fantazzk.ApiResponse;
import com.naminhyeok.fantazzk.room.GameView;
import com.naminhyeok.fantazzk.room.RoomStartApi;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/rooms")
@RequiredArgsConstructor
class RoomStartApiController {
    private final RoomStartApi roomStartApi;

    @PostMapping("/{code}/start")
    ApiResponse<GameView> start(
        @PathVariable String code,
        @RequestHeader(value = "X-Room-Action-Token", required = false) String actionToken
    ) {
        return ApiResponse.success(roomStartApi.start(code, actionToken));
    }
}
