package com.naminhyeok.fantazzk.room;

import com.naminhyeok.fantazzk.ApiResponse;
import com.naminhyeok.fantazzk.OpenApiDocumentation;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/rooms")
@RequiredArgsConstructor
@Tag(name = OpenApiDocumentation.ROOM_LOBBY_TAG)
class RoomQueryApiController {
    private final GetRoom getRoom;
    private final FindJoinableRooms findJoinableRooms;

    @GetMapping
    @Operation(
        summary = "참가 가능한 방 목록 조회",
        description = "웹 메인 화면에서 노출할 최신 대기방 목록을 조회합니다. 전체 목록이 아니라 참가 가능한 최신 방만 최대 5개 반환합니다."
    )
    ApiResponse<List<JoinableRoomResponse>> list() {
        return ApiResponse.success(findJoinableRooms.list());
    }

    @GetMapping("/{code}")
    @Operation(
        summary = "로비 상태 조회",
        description = "로비 화면의 source of truth 입니다. 방이 시작된 뒤에는 `startedGameId` 값을 보고 `/games/{gameId}` 기반 화면으로 전환해야 합니다."
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "로비 조회 성공",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(value = OpenApiDocumentation.ROOM_VIEW_SUCCESS_EXAMPLE)
            )
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "404",
            description = "방을 찾을 수 없음"
        )
    })
    ApiResponse<RoomViewResponse> getByCode(
        @Parameter(description = OpenApiDocumentation.ROOM_CODE_DESCRIPTION, example = "ROOM01")
        @PathVariable String code
    ) {
        return ApiResponse.success(RoomViewResponse.from(getRoom.get(code)));
    }
}
