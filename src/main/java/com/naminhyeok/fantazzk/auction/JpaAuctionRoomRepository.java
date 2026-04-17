package com.naminhyeok.fantazzk.auction;

import java.util.List;
import java.util.Optional;
import org.springframework.data.repository.Repository;

interface JpaAuctionRoomRepository extends Repository<JpaAuctionRoomEntity, String> {
    JpaAuctionRoomEntity save(JpaAuctionRoomEntity entity);

    Optional<JpaAuctionRoomEntity> findById(String roomCode);

    List<JpaAuctionRoomEntity> findAll();
}
