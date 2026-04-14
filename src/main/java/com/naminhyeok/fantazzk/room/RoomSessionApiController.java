package com.naminhyeok.fantazzk.room;

import com.naminhyeok.fantazzk.ApiResponse;
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
    private final CreateRoom createRoom;
    private final JoinRoom joinRoom;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    ApiResponse<RoomSessionResponse> create(@Valid @RequestBody CreateRoomRequest request) {
        return ApiResponse.success(RoomSessionResponse.from(createRoom.create(request.templateId(), request.hostNickname())));
    }

    @PostMapping("/{code}/join")
    ApiResponse<RoomSessionResponse> join(@PathVariable String code, @Valid @RequestBody JoinRoomRequest request) {
        return ApiResponse.success(RoomSessionResponse.from(joinRoom.join(code, request.nickname())));
    }
}
