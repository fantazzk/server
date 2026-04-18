package com.naminhyeok.fantazzk.room.web.room;

import com.naminhyeok.fantazzk.ApiResponse;
import com.naminhyeok.fantazzk.room.RoomSessionApi;
import com.naminhyeok.fantazzk.room.RoomSessionView;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/rooms")
@RequiredArgsConstructor
class RoomSessionApiController {
    private final RoomSessionApi roomSessionApi;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    ApiResponse<RoomSessionView> create(@Valid @RequestBody CreateRoomRequest request) {
        return ApiResponse.success(roomSessionApi.create(request.templateId(), request.hostNickname()));
    }

    @PostMapping("/{code}/join")
    ApiResponse<RoomSessionView> join(@PathVariable String code, @Valid @RequestBody JoinRoomRequest request) {
        return ApiResponse.success(roomSessionApi.join(code, request.nickname()));
    }
}
