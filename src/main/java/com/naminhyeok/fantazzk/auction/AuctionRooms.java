package com.naminhyeok.fantazzk.auction;

import java.util.List;
import java.util.Optional;
import org.jmolecules.ddd.annotation.Repository;

@Repository
interface AuctionRooms {
    AuctionRoom save(AuctionRoom room);

    AuctionRoom saveAndFlush(AuctionRoom room);

    Optional<AuctionRoom> findByCode(String code);

    List<AuctionRoom> findInProgressAuctionRooms();
}
