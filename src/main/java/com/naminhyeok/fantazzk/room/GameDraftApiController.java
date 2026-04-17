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
class GameDraftApiController {
    private final PickDraft pickDraft;
    private final GetGame getGame;

    @PostMapping("/{gameId}/draft-picks")
    ApiResponse<GameResponse> pickDraft(
        @PathVariable UUID gameId,
        @RequestHeader(value = "X-Room-Action-Token", required = false) String actionToken,
        @Valid @RequestBody PickDraftRequest request
    ) {
        pickDraft.pick(gameId, actionToken, request.playerName());
        return ApiResponse.success(GameResponse.from(getGame.get(gameId)));
    }
}
