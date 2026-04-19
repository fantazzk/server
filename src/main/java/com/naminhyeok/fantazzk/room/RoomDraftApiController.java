package com.naminhyeok.fantazzk.room;

import com.naminhyeok.fantazzk.ApiResponse;
import com.naminhyeok.fantazzk.OpenApiDocumentation;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = OpenApiDocumentation.ROOM_LOBBY_TAG)
class RoomDraftApiController {
    private final SelectDraftPosition selectDraftPosition;
    private final ClearDraftPosition clearDraftPosition;

    @PutMapping("/{code}/draft-position")
    @Operation(
        summary = "드래프트 자리 선택 또는 변경",
        description = "드래프트 모드 로비에서 현재 팀장의 드래프트 순번 선택 요청을 전달합니다. 성공 시에는 빈 성공 응답만 반환하며, 로비 갱신은 realtime 이벤트나 GET /rooms/{code}로 확인합니다.",
        security = @SecurityRequirement(name = OpenApiDocumentation.ROOM_ACTION_TOKEN_SCHEME)
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "자리 선택 성공",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(value = OpenApiDocumentation.EMPTY_SUCCESS_EXAMPLE)
            )
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "401",
            description = "액션 토큰이 없거나 유효하지 않음",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(value = OpenApiDocumentation.ROOM_ACTION_TOKEN_REQUIRED_EXAMPLE)
            )
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "409",
            description = "이미 선점된 자리이거나 대기 상태가 아님"
        )
    })
    ApiResponse<Void> selectDraftPosition(
        @Parameter(description = OpenApiDocumentation.ROOM_CODE_DESCRIPTION, example = "ROOM01")
        @PathVariable String code,
        @Parameter(description = OpenApiDocumentation.ROOM_ACTION_TOKEN_DESCRIPTION)
        @RequestHeader(value = OpenApiDocumentation.ROOM_ACTION_TOKEN_HEADER, required = false) String actionToken,
        @Valid @RequestBody SelectDraftPositionRequest request
    ) {
        selectDraftPosition.select(code, actionToken, request.draftPosition());
        return ApiResponse.success();
    }

    @DeleteMapping("/{code}/draft-position")
    @Operation(
        summary = "드래프트 자리 선택 취소",
        description = "현재 팀장이 확정했던 드래프트 순번 해제 요청을 전달합니다. 성공 시에는 빈 성공 응답만 반환하며, 로비 갱신은 realtime 이벤트나 GET /rooms/{code}로 확인합니다.",
        security = @SecurityRequirement(name = OpenApiDocumentation.ROOM_ACTION_TOKEN_SCHEME)
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "자리 선택 취소 성공",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(value = OpenApiDocumentation.EMPTY_SUCCESS_EXAMPLE)
            )
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "401",
            description = "액션 토큰이 없거나 유효하지 않음",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(value = OpenApiDocumentation.ROOM_ACTION_TOKEN_REQUIRED_EXAMPLE)
            )
        )
    })
    ApiResponse<Void> clearDraftPosition(
        @Parameter(description = OpenApiDocumentation.ROOM_CODE_DESCRIPTION, example = "ROOM01")
        @PathVariable String code,
        @Parameter(description = OpenApiDocumentation.ROOM_ACTION_TOKEN_DESCRIPTION)
        @RequestHeader(value = OpenApiDocumentation.ROOM_ACTION_TOKEN_HEADER, required = false) String actionToken
    ) {
        clearDraftPosition.clear(code, actionToken);
        return ApiResponse.success();
    }
}
