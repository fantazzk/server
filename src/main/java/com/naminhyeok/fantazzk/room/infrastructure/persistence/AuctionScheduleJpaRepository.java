package com.naminhyeok.fantazzk.room.infrastructure.persistence;

import com.naminhyeok.fantazzk.room.application.query.AuctionScheduleCandidate;
import jakarta.persistence.EntityManager;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
class AuctionScheduleJpaRepository {
    private final EntityManager entityManager;

    List<AuctionScheduleCandidate> findInProgressAuctionSchedules(int limit, int offset) {
        @SuppressWarnings("unchecked")
        List<Object[]> rows = entityManager.createNativeQuery(
            """
                select g.room_code, g.current_round_ends_at
                from games g
                where g.game_mode = 'AUCTION'
                  and g.current_round_ends_at is not null
                order by g.room_code asc
                """
        )
            .setFirstResult(offset)
            .setMaxResults(limit)
            .getResultList();
        return rows.stream()
            .map(row -> new AuctionScheduleCandidate((String) row[0], toInstant(row[1])))
            .toList();
    }

    private Instant toInstant(Object rawDeadline) {
        if (rawDeadline instanceof OffsetDateTime offsetDateTime) {
            return offsetDateTime.toInstant();
        }
        if (rawDeadline instanceof java.sql.Timestamp timestamp) {
            return timestamp.toInstant();
        }
        throw new IllegalStateException("지원하지 않는 current_round_ends_at 타입입니다: " + rawDeadline.getClass().getName());
    }
}
