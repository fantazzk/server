package com.naminhyeok.fantazzk.room;

import com.naminhyeok.fantazzk.ApiResponse;
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
    private final GetGame getGame;

    @GetMapping("/{gameId}")
    ApiResponse<GameResponse> get(@PathVariable UUID gameId) {
        return ApiResponse.success(GameResponse.from(getGame.get(gameId)));
    }
}
