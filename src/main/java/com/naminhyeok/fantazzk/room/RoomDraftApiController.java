package com.naminhyeok.fantazzk.room;

import com.naminhyeok.fantazzk.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/rooms")
@RequiredArgsConstructor
class RoomDraftApiController {
    private final PickDraft pickDraft;
    private final SelectDraftPosition selectDraftPosition;
    private final ClearDraftPosition clearDraftPosition;

    @PostMapping("/{code}/draft-picks")
    ApiResponse<RoomResponse> pickDraft(
        @PathVariable String code,
        @RequestHeader(value = "X-Room-Action-Token", required = false) String actionToken,
        @Valid @RequestBody PickDraftRequest request
    ) {
        return ApiResponse.success(RoomResponse.from(pickDraft.pick(code, actionToken, request.playerName())));
    }

    @PutMapping("/{code}/draft-position")
    ApiResponse<RoomResponse> selectDraftPosition(
        @PathVariable String code,
        @RequestHeader(value = "X-Room-Action-Token", required = false) String actionToken,
        @Valid @RequestBody SelectDraftPositionRequest request
    ) {
        return ApiResponse.success(RoomResponse.from(selectDraftPosition.select(code, actionToken, request.draftPosition())));
    }

    @DeleteMapping("/{code}/draft-position")
    ApiResponse<RoomResponse> clearDraftPosition(
        @PathVariable String code,
        @RequestHeader(value = "X-Room-Action-Token", required = false) String actionToken
    ) {
        return ApiResponse.success(RoomResponse.from(clearDraftPosition.clear(code, actionToken)));
    }
}
