package com.naminhyeok.fantazzk.room;

import com.naminhyeok.fantazzk.CoreException;
import com.naminhyeok.fantazzk.template.TemplateId;
import java.util.Map;

public class RoomException extends CoreException {
    private RoomException(RoomErrorType error, Object data) {
        super(error, data);
    }

    public static RoomException notFound(String code) {
        return new RoomException(RoomErrorType.ROOM_NOT_FOUND, Map.of("code", code));
    }

    public static RoomException templateNotFound(TemplateId templateId) {
        return new RoomException(
            RoomErrorType.ROOM_TEMPLATE_NOT_FOUND,
            Map.of("templateId", templateId.templateId().toString())
        );
    }

    public static RoomException notJoinable(String detail) {
        return new RoomException(RoomErrorType.ROOM_NOT_JOINABLE, Map.of("detail", detail));
    }

    public static RoomException roomFull() {
        return new RoomException(RoomErrorType.ROOM_FULL, Map.of("detail", RoomErrorType.ROOM_FULL.message()));
    }

    public static RoomException notStartable(String detail) {
        return new RoomException(RoomErrorType.ROOM_NOT_STARTABLE, Map.of("detail", detail));
    }
}
