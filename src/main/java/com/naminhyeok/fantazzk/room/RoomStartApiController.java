package com.naminhyeok.fantazzk.room;

import com.naminhyeok.fantazzk.ApiResponse;
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
    private final StartRoom startRoom;

    @PostMapping("/{code}/start")
    ApiResponse<GameResponse> start(
        @PathVariable String code,
        @RequestHeader(value = "X-Room-Action-Token", required = false) String actionToken
    ) {
        return ApiResponse.success(GameResponse.from(startRoom.start(code, actionToken)));
    }
}
