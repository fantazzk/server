package com.naminhyeok.fantazzk.room.infrastructure.persistence;

import com.naminhyeok.fantazzk.room.application.query.JoinableRoomSummary;
import jakarta.persistence.EntityManager;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
class JoinableRoomJpaRepository {
    private final EntityManager entityManager;

    List<JoinableRoomSummary> findLatestWaitingRooms(int limit, int offset) {
        @SuppressWarnings("unchecked")
        List<Object[]> rows = entityManager.createNativeQuery(
            """
                select r.code, r.mode, r.team_count, count(l.team_leader_id) as joined_leader_count
                from rooms r
                left join room_team_leader l on l.leaders_room_id = r.room_id
                where r.status = 'WAITING'
                group by r.room_id, r.code, r.mode, r.team_count, r.created_at
                having count(l.team_leader_id) < r.team_count
                order by r.created_at desc, r.code desc
                """
        )
            .setFirstResult(offset)
            .setMaxResults(limit)
            .getResultList();
        return rows.stream().map(this::toView).toList();
    }

    private JoinableRoomSummary toView(Object[] row) {
        String code = (String) row[0];
        String mode = row[1].toString();
        int teamCount = ((Number) row[2]).intValue();
        int joinedLeaderCount = ((Number) row[3]).intValue();
        return new JoinableRoomSummary(code, mode, teamCount, joinedLeaderCount, teamCount - joinedLeaderCount, "WAITING_FOR_LEADERS");
    }
}
