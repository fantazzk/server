package com.naminhyeok.fantazzk.room.repository

import com.naminhyeok.fantazzk.room.RoomBid
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
    override fun save(bid: RoomBid): RoomBid {
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
        return roomBidJdbcCrudRepository.save(entity).toDomain()
    }

    override fun findByRoomIdAndRound(
        roomId: Long,
        round: Int,
    ): List<RoomBid> = roomBidJdbcCrudRepository.findByRoomIdAndRound(roomId, round).map { it.toDomain() }

    override fun findHighestByRoomIdAndRound(
        roomId: Long,
        round: Int,
    ): RoomBid? = roomBidJdbcCrudRepository.findFirstByRoomIdAndRoundOrderByAmountDesc(roomId, round)?.toDomain()

    private fun RoomBidEntity.toDomain() =
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
