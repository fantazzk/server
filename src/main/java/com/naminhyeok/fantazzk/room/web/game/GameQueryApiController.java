package com.naminhyeok.fantazzk.room.web.game;

import com.naminhyeok.fantazzk.ApiResponse;
import com.naminhyeok.fantazzk.room.GameQueryApi;
import com.naminhyeok.fantazzk.room.GameView;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/games")
@RequiredArgsConstructor
class GameQueryApiController {
    private final GameQueryApi gameQueryApi;

    @GetMapping("/{gameId}")
    ApiResponse<GameView> get(@PathVariable UUID gameId) {
        return ApiResponse.success(gameQueryApi.get(gameId));
    }
}
