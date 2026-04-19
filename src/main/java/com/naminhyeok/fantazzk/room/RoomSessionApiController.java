package com.naminhyeok.fantazzk.room;

import com.naminhyeok.fantazzk.ApiResponse;
import com.naminhyeok.fantazzk.OpenApiDocumentation;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = OpenApiDocumentation.ROOM_SESSION_TAG)
class RoomSessionApiController {
    private final CreateRoom createRoom;
    private final JoinRoom joinRoom;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(
        summary = "방 생성",
        description = "템플릿을 기준으로 새 방을 만들고, 즉시 호스트 세션을 발급합니다. 응답의 `teamLeaderSession.actionToken` 은 이후 모든 로비/게임 mutation 요청에 사용해야 합니다."
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "201",
            description = "방 생성 성공",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(value = OpenApiDocumentation.ROOM_SESSION_SUCCESS_EXAMPLE)
            )
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "400",
            description = "요청 validation 실패",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(value = OpenApiDocumentation.BAD_REQUEST_VALIDATION_EXAMPLE)
            )
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "404",
            description = "템플릿을 찾을 수 없음"
        )
    })
    ApiResponse<RoomCreateResponse> create(@Valid @RequestBody CreateRoomRequest request) {
        return ApiResponse.success(RoomCreateResponse.from(createRoom.create(request.templateId(), request.hostNickname())));
    }

    @PostMapping("/{code}/join")
    @Operation(
        summary = "방 참가",
        description = "공유받은 방 코드로 로비에 참가하고, 참가자 세션을 발급합니다. 성공 시 반환되는 `actionToken` 을 저장해야 이후 자리 선택, 픽, 입찰을 수행할 수 있습니다."
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "방 참가 성공",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(value = OpenApiDocumentation.ROOM_JOIN_SUCCESS_EXAMPLE)
            )
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "400",
            description = "요청 validation 실패",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(value = OpenApiDocumentation.BAD_REQUEST_JOIN_VALIDATION_EXAMPLE)
            )
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "404",
            description = "방을 찾을 수 없음"
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "409",
            description = "방이 가득 찼거나 닉네임이 중복됨",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(value = OpenApiDocumentation.ROOM_NICKNAME_ALREADY_TAKEN_EXAMPLE)
            )
        )
    })
    ApiResponse<RoomJoinResponse> join(
        @Parameter(description = OpenApiDocumentation.ROOM_CODE_DESCRIPTION, example = "ROOM01")
        @PathVariable String code,
        @Valid @RequestBody JoinRoomRequest request
    ) {
        return ApiResponse.success(RoomJoinResponse.from(joinRoom.join(code, request.nickname())));
    }
}
