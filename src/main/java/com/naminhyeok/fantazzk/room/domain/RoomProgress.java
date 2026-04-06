package com.naminhyeok.fantazzk.room.domain;

public sealed interface RoomProgress
    permits RoomProgress.WaitingProgress, RoomProgress.Auction, RoomProgress.Draft, RoomProgress.CompletedProgress {

    RoomStatus status();

    RoomProgress Waiting = new WaitingProgress();
    RoomProgress Completed = new CompletedProgress();

    static RoomProgress from(Room room) {
        return switch (room.getStatus()) {
            case WAITING -> Waiting;
            case COMPLETED -> Completed;
            case IN_PROGRESS -> {
                if (room.getMode() == TeamBuildingMode.AUCTION) {
                    Integer currentAuctionRound = room.getCurrentAuctionRound();
                    if (currentAuctionRound == null) {
                        throw new IllegalArgumentException("현재 라운드가 존재해야 합니다");
                    }
                    yield new Auction(
                        currentAuctionRound
                    );
                }

                Integer currentTurnIndex = room.getCurrentTurnIndex();
                if (currentTurnIndex == null) {
                    throw new IllegalArgumentException("현재 턴이 존재해야 합니다");
                }
                yield new Draft(
                    currentTurnIndex
                );
            }
        };
    }

    record Auction(int currentRound) implements RoomProgress {
        @Override
        public RoomStatus status() {
            return RoomStatus.IN_PROGRESS;
        }
    }

    record Draft(int currentTurnIndex) implements RoomProgress {
        @Override
        public RoomStatus status() {
            return RoomStatus.IN_PROGRESS;
        }
    }

    final class WaitingProgress implements RoomProgress {
        private WaitingProgress() {
        }

        @Override
        public RoomStatus status() {
            return RoomStatus.WAITING;
        }
    }

    final class CompletedProgress implements RoomProgress {
        private CompletedProgress() {
        }

        @Override
        public RoomStatus status() {
            return RoomStatus.COMPLETED;
        }
    }
}
