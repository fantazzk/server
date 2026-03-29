package com.naminhyeok.fantazzk.room

import com.naminhyeok.fantazzk.room.dto.CreateRoomRequest
import com.naminhyeok.fantazzk.room.dto.JoinRoomRequest
import com.naminhyeok.fantazzk.room.dto.PickRequest
import com.naminhyeok.fantazzk.room.dto.PlaceBidRequest
import com.naminhyeok.fantazzk.room.dto.RoomResponse
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
    private val roomCreateService: RoomCreateService,
    private val roomLookUpService: RoomLookUpService,
    private val roomJoinService: RoomJoinService,
    private val roomStartService: RoomStartService,
    private val auctionService: AuctionService,
    private val draftService: DraftService,
) {
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "방 생성", operationId = "createRoom")
    fun create(@RequestBody request: CreateRoomRequest): RoomResponse {
        val room = roomCreateService.create(request.templateId, request.hostNickname)
        val leaders = roomLookUpService.getTeamLeaders(room.roomId)
        return RoomResponse.from(room, leaders)
    }

    @GetMapping("/{code}")
    @Operation(summary = "방 조회", operationId = "getRoom")
    fun getByCode(@PathVariable code: String): RoomResponse {
        val room = roomLookUpService.get(code)
        val leaders = roomLookUpService.getTeamLeaders(room.roomId)
        return RoomResponse.from(room, leaders)
    }

    @PostMapping("/{code}/join")
    @Operation(summary = "방 참가", operationId = "joinRoom")
    fun join(@PathVariable code: String, @RequestBody request: JoinRoomRequest): RoomResponse {
        roomJoinService.join(code, request.nickname)
        val room = roomLookUpService.get(code)
        val leaders = roomLookUpService.getTeamLeaders(room.roomId)
        return RoomResponse.from(room, leaders)
    }

    @PostMapping("/{code}/start")
    @Operation(summary = "방 시작", operationId = "startRoom")
    fun start(@PathVariable code: String): RoomResponse {
        roomStartService.start(code)
        val room = roomLookUpService.get(code)
        val leaders = roomLookUpService.getTeamLeaders(room.roomId)
        return RoomResponse.from(room, leaders)
    }

    @PostMapping("/{code}/bid")
    @Operation(summary = "입찰", operationId = "placeBid")
    fun placeBid(@PathVariable code: String, @RequestBody request: PlaceBidRequest): RoomResponse {
        auctionService.placeBid(code, request.teamLeaderId, request.amount)
        val room = roomLookUpService.get(code)
        val leaders = roomLookUpService.getTeamLeaders(room.roomId)
        return RoomResponse.from(room, leaders)
    }

    @PostMapping("/{code}/settle")
    @Operation(summary = "경매 정산", operationId = "settleAuction")
    fun settle(@PathVariable code: String): RoomResponse {
        auctionService.settle(code)
        val room = roomLookUpService.get(code)
        val leaders = roomLookUpService.getTeamLeaders(room.roomId)
        return RoomResponse.from(room, leaders)
    }

    @PostMapping("/{code}/pick")
    @Operation(summary = "드래프트 픽", operationId = "pick")
    fun pick(@PathVariable code: String, @RequestBody request: PickRequest): RoomResponse {
        draftService.pick(code, request.teamLeaderId, request.playerName)
        val room = roomLookUpService.get(code)
        val leaders = roomLookUpService.getTeamLeaders(room.roomId)
        return RoomResponse.from(room, leaders)
    }
}
