package com.naminhyeok.fantazzk.teambuilding.room.repository

import com.naminhyeok.fantazzk.teambuilding.room.RoomBid
import com.naminhyeok.fantazzk.teambuilding.room.RoomBidModel
import org.springframework.data.repository.CrudRepository

interface RoomBidJdbcCrudRepository : CrudRepository<RoomBidEntity, Long> {
    fun findByRoomIdAndRound(
        roomId: Long,
        round: Int,
    ): List<RoomBidEntity>

    fun findFirstByRoomIdAndRoundOrderByAmountDesc(
        roomId: Long,
        round: Int,
    ): RoomBidEntity?
}

class RoomBidRepositoryImpl(
    private val roomBidJdbcCrudRepository: RoomBidJdbcCrudRepository,
) : RoomBidRepository {
    override fun save(bid: RoomBid): RoomBidModel {
        val entity =
            RoomBidEntity(
                roomId = bid.roomId,
                round = bid.round,
                teamLeaderId = bid.teamLeaderId,
                amount = bid.amount,
                createdAt = bid.createdAt,
                updatedAt = bid.updatedAt,
            )
        if (bid.roomBidId != 0L) entity.id = bid.roomBidId
        return roomBidJdbcCrudRepository.save(entity).toModel()
    }

    override fun findByRoomIdAndRound(
        roomId: Long,
        round: Int,
    ): List<RoomBidModel> = roomBidJdbcCrudRepository.findByRoomIdAndRound(roomId, round).map { it.toModel() }

    override fun findHighestByRoomIdAndRound(
        roomId: Long,
        round: Int,
    ): RoomBidModel? = roomBidJdbcCrudRepository.findFirstByRoomIdAndRoundOrderByAmountDesc(roomId, round)?.toModel()

    private fun RoomBidEntity.toModel() =
        RoomBid(
            roomBidId = id,
            roomId = roomId,
            round = round,
            teamLeaderId = teamLeaderId,
            amount = amount,
            createdAt = createdAt,
            updatedAt = updatedAt,
        )
}
