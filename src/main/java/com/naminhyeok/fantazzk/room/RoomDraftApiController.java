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
    private final SelectDraftPosition selectDraftPosition;
    private final ClearDraftPosition clearDraftPosition;
    private final GetRoom getRoom;

    @PutMapping("/{code}/draft-position")
    ApiResponse<RoomViewResponse> selectDraftPosition(
        @PathVariable String code,
        @RequestHeader(value = "X-Room-Action-Token", required = false) String actionToken,
        @Valid @RequestBody SelectDraftPositionRequest request
    ) {
        selectDraftPosition.select(code, actionToken, request.draftPosition());
        return ApiResponse.success(RoomViewResponse.from(getRoom.get(code)));
    }

    @DeleteMapping("/{code}/draft-position")
    ApiResponse<RoomViewResponse> clearDraftPosition(
        @PathVariable String code,
        @RequestHeader(value = "X-Room-Action-Token", required = false) String actionToken
    ) {
        clearDraftPosition.clear(code, actionToken);
        return ApiResponse.success(RoomViewResponse.from(getRoom.get(code)));
    }
}
