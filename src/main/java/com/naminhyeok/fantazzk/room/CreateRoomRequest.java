package com.naminhyeok.fantazzk.room;

import com.naminhyeok.fantazzk.template.TemplateId;
import jakarta.validation.constraints.NotBlank;
import java.util.UUID;

public record CreateRoomRequest(
    @NotBlank(message = "템플릿 ID는 비어 있을 수 없습니다") String templateId,
    @NotBlank(message = "호스트 이름은 비어 있을 수 없습니다") String hostNickname
) {
    TemplateId templateIdAsIdentifier() {
        return new TemplateId(UUID.fromString(templateId));
    }
}
