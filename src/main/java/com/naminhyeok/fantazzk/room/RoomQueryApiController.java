package com.naminhyeok.fantazzk.room;

import com.naminhyeok.fantazzk.ApiResponse;
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
    private final GetRoomDetails getRoomDetails;
    private final FindJoinableRooms findJoinableRooms;

    @GetMapping
    ApiResponse<List<JoinableRoomResponse>> list() {
        return ApiResponse.success(findJoinableRooms.list());
    }

    @GetMapping("/{code}")
    ApiResponse<RoomResponse> getByCode(@PathVariable String code) {
        return ApiResponse.success(RoomResponse.from(getRoomDetails.get(code)));
    }
}
