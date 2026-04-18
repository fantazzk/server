package com.naminhyeok.fantazzk.room.web.game;

import com.naminhyeok.fantazzk.ApiResponse;
import com.naminhyeok.fantazzk.room.GameDraftApi;
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
class GameDraftApiController {
    private final GameDraftApi gameDraftApi;

    @PostMapping("/{gameId}/draft-picks")
    ApiResponse<GameView> pickDraft(
        @PathVariable UUID gameId,
        @RequestHeader(value = "X-Room-Action-Token", required = false) String actionToken,
        @Valid @RequestBody PickDraftRequest request
    ) {
        return ApiResponse.success(gameDraftApi.pick(gameId, actionToken, request.playerName()));
    }
}
