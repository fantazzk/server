package com.naminhyeok.fantazzk.room.infrastructure.persistence;

import static org.assertj.core.api.Assertions.assertThat;

import com.naminhyeok.fantazzk.room.domain.AuctionGame;
import com.naminhyeok.fantazzk.room.domain.GameId;
import com.naminhyeok.fantazzk.room.domain.GameRules;
import com.naminhyeok.fantazzk.room.domain.RoomId;
import com.naminhyeok.fantazzk.room.query.AuctionScheduleCandidate;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Pageable;

class JpaAuctionScheduleReaderTest {
    @Test
    void 경매_마감_후보를_모든_페이지에서_읽는다() {
        JpaAuctionScheduleReader reader = new JpaAuctionScheduleReader(new RecordingScheduleRepository(205));

        List<AuctionScheduleCandidate> candidates = reader.findInProgressAuctionSchedules();

        assertThat(candidates).hasSize(205);
        assertThat(candidates).extracting(AuctionScheduleCandidate::code)
            .contains("ROOM000", "ROOM204");
    }

    private static final class RecordingScheduleRepository implements AuctionScheduleJpaRepository {
        private final List<AuctionGame> games;

        private RecordingScheduleRepository(int count) {
            this.games = new ArrayList<>();
            for (int index = 0; index < count; index++) {
                games.add(auctionGame("ROOM%03d".formatted(index), Instant.parse("2026-04-09T00:00:00Z").plusSeconds(index)));
            }
        }

        @Override
        public List<AuctionGame> findByCurrentRoundEndsAtNotNullOrderByRoomCodeAsc(Pageable pageable) {
            int fromIndex = (int) pageable.getOffset();
            if (fromIndex >= games.size()) {
                return List.of();
            }
            int toIndex = Math.min(fromIndex + pageable.getPageSize(), games.size());
            return games.subList(fromIndex, toIndex);
        }
    }

    private static AuctionGame auctionGame(String code, Instant deadline) {
        return new AuctionGame(
            deterministicGameId("game:" + code),
            new RoomId(deterministicUuid("room:" + code)),
            code,
            "LEAGUE_OF_LEGENDS",
            Instant.parse("2026-04-09T00:00:00Z"),
            GameRules.auction(2, 2, 300, 30, 10),
            List.of(),
            List.of(),
            1,
            deadline
        );
    }

    private static GameId deterministicGameId(String source) {
        return new GameId(deterministicUuid(source));
    }

    private static UUID deterministicUuid(String source) {
        return UUID.nameUUIDFromBytes(source.getBytes(StandardCharsets.UTF_8));
    }
}
