package com.naminhyeok.fantazzk.draft;

import java.util.Optional;
import org.jmolecules.ddd.annotation.Repository;

@Repository
interface DraftRooms extends org.jmolecules.ddd.types.Repository<DraftRoom, DraftRoomId> {
    DraftRoom save(DraftRoom draftRoom);

    Optional<DraftRoom> findById(DraftRoomId id);
}
