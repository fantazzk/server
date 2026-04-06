package com.naminhyeok.fantazzk.room.domain;

import java.util.Objects;

public record BudgetState(int remainingBudget) {
    public BudgetState {
        if (remainingBudget < 0) {
            throw new IllegalArgumentException("예산은 0 이상이어야 합니다");
        }
    }

    public void requireCanBid(int amount) {
        requireNonNegativeAmount(amount);
        if (amount > remainingBudget) {
            throw new IllegalArgumentException("예산이 부족합니다: 잔여 " + remainingBudget + ", 필요 " + amount);
        }
    }

    public BudgetState spend(int amount) {
        requireCanBid(amount);
        return new BudgetState(remainingBudget - amount);
    }

    public static BudgetState from(Integer remainingBudget) {
        return remainingBudget == null ? null : new BudgetState(remainingBudget);
    }

    public static BudgetState requireFrom(Integer remainingBudget) {
        BudgetState state = from(remainingBudget);
        if (state == null) {
            throw new IllegalArgumentException("이 모드에서는 예산이 존재하지 않습니다");
        }
        return state;
    }

    private static void requireNonNegativeAmount(int amount) {
        if (amount < 0) {
            throw new IllegalArgumentException("금액은 0 이상이어야 합니다");
        }
    }
}
