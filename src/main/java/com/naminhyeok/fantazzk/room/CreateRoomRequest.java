package com.naminhyeok.fantazzk.room;

import com.naminhyeok.fantazzk.template.TemplateId;
import java.util.UUID;

public record CreateRoomRequest(
    String templateId,
    String hostNickname
) {
    TemplateId templateIdAsIdentifier() {
        return new TemplateId(UUID.fromString(templateId));
    }
}
