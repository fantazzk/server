package com.naminhyeok.fantazzk.room.web;

import com.naminhyeok.fantazzk.ApiResponse;
import com.naminhyeok.fantazzk.room.RoomManagement;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/rooms")
@RequiredArgsConstructor
class RoomApiController {
    private final RoomManagement roomManagement;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    ApiResponse<RoomResponse> create(@Valid @RequestBody CreateRoomRequest request) {
        return ApiResponse.success(RoomResponse.from(roomManagement.create(request.templateIdAsUuid(), request.hostNickname())));
    }

    @GetMapping("/{code}")
    ApiResponse<RoomResponse> getByCode(@PathVariable String code) {
        return ApiResponse.success(RoomResponse.from(roomManagement.get(code)));
    }

    @PostMapping("/{code}/join")
    ApiResponse<RoomResponse> join(@PathVariable String code, @Valid @RequestBody JoinRoomRequest request) {
        return ApiResponse.success(RoomResponse.from(roomManagement.join(code, request.nickname())));
    }

    @PostMapping("/{code}/start")
    ApiResponse<RoomResponse> start(@PathVariable String code) {
        return ApiResponse.success(RoomResponse.from(roomManagement.start(code)));
    }
}
