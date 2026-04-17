package com.naminhyeok.fantazzk.auction;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Repository;

@Repository
class JpaAuctionRooms implements AuctionRooms {
    private final JpaAuctionRoomRepository repository;
    private final ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();

    JpaAuctionRooms(JpaAuctionRoomRepository repository) {
        this.repository = repository;
    }

    @Override
    public AuctionRoom save(AuctionRoom room) {
        repository.save(new JpaAuctionRoomEntity(room.readState().code(), writeSnapshot(room.snapshot())));
        return room;
    }

    @Override
    public AuctionRoom saveAndFlush(AuctionRoom room) {
        return save(room);
    }

    @Override
    public Optional<AuctionRoom> findByCode(String code) {
        return repository.findById(code)
            .map(JpaAuctionRoomEntity::snapshotJson)
            .map(this::readSnapshot)
            .map(AuctionRoom::restore);
    }

    @Override
    public List<AuctionRoom> findInProgressAuctionRooms() {
        return repository.findAll().stream()
            .map(JpaAuctionRoomEntity::snapshotJson)
            .map(this::readSnapshot)
            .map(AuctionRoom::restore)
            .filter(room -> room.readState().status() == AuctionRoomStatus.IN_PROGRESS)
            .toList();
    }

    private String writeSnapshot(AuctionRoomSnapshot snapshot) {
        try {
            return objectMapper.writeValueAsString(snapshot);
        } catch (JsonProcessingException ex) {
            throw new IllegalStateException("경매 상태를 직렬화하지 못했습니다", ex);
        }
    }

    private AuctionRoomSnapshot readSnapshot(String snapshotJson) {
        try {
            return objectMapper.readValue(snapshotJson, AuctionRoomSnapshot.class);
        } catch (JsonProcessingException ex) {
            throw new IllegalStateException("경매 상태를 역직렬화하지 못했습니다", ex);
        }
    }
}
