package com.naminhyeok.fantazzk.draft;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Optional;
import org.springframework.stereotype.Repository;

@Repository
class JpaDraftRooms implements DraftRooms {
    private final JpaDraftRoomRepository repository;
    private final ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();

    JpaDraftRooms(JpaDraftRoomRepository repository) {
        this.repository = repository;
    }

    @Override
    public DraftRoom save(DraftRoom draftRoom) {
        repository.save(new JpaDraftRoomEntity(draftRoom.getId().value(), writeState(draftRoom.snapshot())));
        return draftRoom;
    }

    @Override
    public Optional<DraftRoom> findById(DraftRoomId id) {
        return repository.findById(id.value())
            .map(JpaDraftRoomEntity::stateJson)
            .map(this::readState)
            .map(DraftRoom::restore);
    }

    private String writeState(DraftRoomState state) {
        try {
            return objectMapper.writeValueAsString(state);
        } catch (JsonProcessingException ex) {
            throw new IllegalStateException("드래프트 상태를 직렬화하지 못했습니다", ex);
        }
    }

    private DraftRoomState readState(String stateJson) {
        try {
            return objectMapper.readValue(stateJson, DraftRoomState.class);
        } catch (JsonProcessingException ex) {
            throw new IllegalStateException("드래프트 상태를 역직렬화하지 못했습니다", ex);
        }
    }
}
