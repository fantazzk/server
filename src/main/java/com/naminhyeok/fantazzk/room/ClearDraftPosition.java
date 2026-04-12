package com.naminhyeok.fantazzk.room;

import com.naminhyeok.fantazzk.CoreException;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

@Service
@RequiredArgsConstructor
class ClearDraftPosition {
    private final Rooms rooms;
    private final RoomActionAuthorizer roomActionAuthorizer;
    private final PlatformTransactionManager transactionManager;

    public void clear(String code, String actionToken) {
        inTransaction(() -> {
            Room room = rooms.findByCode(code).orElseThrow(() -> CoreException.of(RoomErrorType.ROOM_NOT_FOUND));
            RoomTeamLeader caller = roomActionAuthorizer.authenticate(room, actionToken);
            room.clearDraftPosition(caller.getId());
            rooms.save(room);
            return room;
        });
    }

    private <T> T inTransaction(TransactionCallback<T> callback) {
        TransactionTemplate transactionTemplate = new TransactionTemplate(transactionManager);
        try {
            return Objects.requireNonNull(transactionTemplate.execute(status -> callback.execute()));
        } catch (OptimisticLockingFailureException ex) {
            throw CoreException.of(RoomErrorType.ROOM_CONCURRENT_MODIFICATION);
        }
    }

    @FunctionalInterface
    private interface TransactionCallback<T> {
        T execute();
    }
}
