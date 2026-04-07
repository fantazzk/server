package com.naminhyeok.fantazzk.room;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class GetRoom {
    private final Rooms rooms;

    @Transactional(readOnly = true)
    public Room get(String code) {
        return rooms.findByCode(code).orElseThrow();
    }
}
