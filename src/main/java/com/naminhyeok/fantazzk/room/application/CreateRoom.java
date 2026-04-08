package com.naminhyeok.fantazzk.room.application;

import com.naminhyeok.fantazzk.CoreException;
import com.naminhyeok.fantazzk.room.domain.Room;
import com.naminhyeok.fantazzk.room.domain.RoomErrorType;
import com.naminhyeok.fantazzk.room.domain.RoomTemplateSpec;
import com.naminhyeok.fantazzk.room.repository.Rooms;
import com.naminhyeok.fantazzk.template.TemplateCatalog;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CreateRoom {
    private final Rooms rooms;
    private final TemplateCatalog templateCatalog;

    @Transactional
    public Room create(UUID templateId, String hostNickname) {
        TemplateCatalog.TemplateBlueprint template = getTemplate(templateId);

        Room room =
            Room.createFromTemplate(
                generateCode(),
                UUID.randomUUID().toString(),
                hostNickname,
                new RoomTemplateSpec(
                    template.mode() == TemplateCatalog.Mode.AUCTION
                        ? RoomTemplateSpec.Mode.AUCTION
                        : RoomTemplateSpec.Mode.DRAFT,
                    template.teamCount(),
                    template.teamSize(),
                    template.budget(),
                    template.draftOrderStrategy() == null
                        ? null
                        : RoomTemplateSpec.DraftOrderStrategy.valueOf(template.draftOrderStrategy().name()),
                    template.players().stream()
                        .map(player -> new RoomTemplateSpec.Player(player.name(), player.displayOrder()))
                        .toList()
                )
            );

        return rooms.save(room);
    }

    private TemplateCatalog.TemplateBlueprint getTemplate(UUID templateId) {
        try {
            return templateCatalog.getTemplate(templateId);
        } catch (TemplateCatalog.NotFound ex) {
            throw CoreException.of(RoomErrorType.ROOM_TEMPLATE_NOT_FOUND);
        }
    }

    private String generateCode() {
        return UUID.randomUUID().toString().replace("-", "").substring(0, 6).toUpperCase();
    }
}
