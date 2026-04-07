package com.naminhyeok.fantazzk.room;

public class RoomTemplateNotFoundException extends RuntimeException {
    public RoomTemplateNotFoundException() {
        super("템플릿을 찾을 수 없습니다");
    }
}
