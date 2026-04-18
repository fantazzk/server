package com.naminhyeok.fantazzk.room.web.room;

import com.naminhyeok.fantazzk.ApiResponse;
import com.naminhyeok.fantazzk.room.RoomDraftApi;
import com.naminhyeok.fantazzk.room.RoomView;
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
    private final RoomDraftApi roomDraftApi;

    @PutMapping("/{code}/draft-position")
    ApiResponse<RoomView> selectDraftPosition(
        @PathVariable String code,
        @RequestHeader(value = "X-Room-Action-Token", required = false) String actionToken,
        @Valid @RequestBody SelectDraftPositionRequest request
    ) {
        return ApiResponse.success(roomDraftApi.selectDraftPosition(code, actionToken, request.draftPosition()));
    }

    @DeleteMapping("/{code}/draft-position")
    ApiResponse<RoomView> clearDraftPosition(
        @PathVariable String code,
        @RequestHeader(value = "X-Room-Action-Token", required = false) String actionToken
    ) {
        return ApiResponse.success(roomDraftApi.clearDraftPosition(code, actionToken));
    }
}
