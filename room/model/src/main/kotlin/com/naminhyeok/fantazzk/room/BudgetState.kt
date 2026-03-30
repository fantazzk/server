package com.naminhyeok.fantazzk.room

data class BudgetState(
    val remainingBudget: Int,
) {
    init {
        require(remainingBudget >= 0) { "예산은 0 이상이어야 합니다" }
    }

    fun requireCanBid(amount: Int) {
        requireNonNegativeAmount(amount)
        require(amount <= remainingBudget) { "예산이 부족합니다: 잔여 $remainingBudget, 필요 $amount" }
    }

    fun spend(amount: Int): BudgetState {
        requireCanBid(amount)
        return BudgetState(remainingBudget = remainingBudget - amount)
    }

    companion object {
        fun from(remainingBudget: Int?): BudgetState? = remainingBudget?.let(::BudgetState)

        fun requireFrom(remainingBudget: Int?): BudgetState = requireNotNull(from(remainingBudget)) { "이 모드에서는 예산이 존재하지 않습니다" }
    }

    private fun requireNonNegativeAmount(amount: Int) {
        require(amount >= 0) { "금액은 0 이상이어야 합니다" }
    }
}
