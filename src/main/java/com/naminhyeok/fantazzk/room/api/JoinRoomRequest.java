package com.naminhyeok.fantazzk.room.api;

import jakarta.validation.constraints.NotBlank;

public record JoinRoomRequest(@NotBlank(message = "닉네임은 비어 있을 수 없습니다") String nickname) {
}
