package com.naminhyeok.fantazzk.room.web;

import jakarta.validation.constraints.NotBlank;

record JoinRoomRequest(@NotBlank(message = "닉네임은 비어 있을 수 없습니다") String nickname) {
}
