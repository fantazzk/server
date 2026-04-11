package com.naminhyeok.fantazzk.room;

import jakarta.validation.constraints.NotBlank;

record PickDraftRequest(
    @NotBlank(message = "선수 이름은 비어 있을 수 없습니다") String playerName
) {
}
