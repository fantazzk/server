package com.naminhyeok.fantazzk.room.web.room;

import com.naminhyeok.fantazzk.ApiResponse;
import com.naminhyeok.fantazzk.room.JoinableRoomView;
import com.naminhyeok.fantazzk.room.RoomQueryApi;
import com.naminhyeok.fantazzk.room.RoomView;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/rooms")
@RequiredArgsConstructor
class RoomQueryApiController {
    private final RoomQueryApi roomQueryApi;

    @GetMapping
    ApiResponse<List<JoinableRoomView>> list() {
        return ApiResponse.success(roomQueryApi.list());
    }

    @GetMapping("/{code}")
    ApiResponse<RoomView> getByCode(@PathVariable String code) {
        return ApiResponse.success(roomQueryApi.get(code));
    }
}
