package com.naminhyeok.fantazzk.teambuilding

import com.naminhyeok.fantazzk.teambuilding.dto.CreateRoomRequest
import com.naminhyeok.fantazzk.teambuilding.dto.JoinRoomRequest
import com.naminhyeok.fantazzk.teambuilding.dto.PickRequest
import com.naminhyeok.fantazzk.teambuilding.dto.PlaceBidRequest
import com.naminhyeok.fantazzk.teambuilding.dto.RoomResponse
import com.naminhyeok.fantazzk.teambuilding.room.TeamLeaderId
import com.naminhyeok.fantazzk.teambuilding.template.TemplateId
import io.swagger.v3.oas.annotations.Operation
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v1/rooms")
class RoomApiController(
    private val roomService: RoomService,
    private val templateService: TemplateService,
) {
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "방 생성", operationId = "createRoom")
    fun create(
        @RequestBody request: CreateRoomRequest,
    ): RoomResponse {
        val template = templateService.get(TemplateId(request.templateId))
        return RoomResponse.from(roomService.create(template, request.hostNickname))
    }

    @GetMapping("/{code}")
    @Operation(summary = "방 조회", operationId = "getRoom")
    fun getByCode(
        @PathVariable code: String,
    ): RoomResponse = RoomResponse.from(roomService.get(code))

    @PostMapping("/{code}/join")
    @Operation(summary = "방 참가", operationId = "joinRoom")
    fun join(
        @PathVariable code: String,
        @RequestBody request: JoinRoomRequest,
    ): RoomResponse = RoomResponse.from(roomService.join(code, request.nickname))

    @PostMapping("/{code}/start")
    @Operation(summary = "방 시작", operationId = "startRoom")
    fun start(
        @PathVariable code: String,
    ): RoomResponse = RoomResponse.from(roomService.start(code))

    @PostMapping("/{code}/bid")
    @Operation(summary = "입찰", operationId = "placeBid")
    fun placeBid(
        @PathVariable code: String,
        @RequestBody request: PlaceBidRequest,
    ): RoomResponse = RoomResponse.from(roomService.placeBid(code, TeamLeaderId(request.teamLeaderId), request.amount))

    @PostMapping("/{code}/settle")
    @Operation(summary = "경매 정산 (낙찰/유찰)", operationId = "settleAuction")
    fun settle(
        @PathVariable code: String,
    ): RoomResponse = RoomResponse.from(roomService.settleAuction(code))

    @PostMapping("/{code}/pick")
    @Operation(summary = "드래프트 픽", operationId = "pick")
    fun pick(
        @PathVariable code: String,
        @RequestBody request: PickRequest,
    ): RoomResponse = RoomResponse.from(roomService.pick(code, TeamLeaderId(request.teamLeaderId), request.playerName))
}
