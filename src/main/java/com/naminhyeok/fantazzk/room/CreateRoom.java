package com.naminhyeok.fantazzk.room;

import com.naminhyeok.fantazzk.template.TemplateCatalog;
import com.naminhyeok.fantazzk.template.TemplateCatalogException;
import com.naminhyeok.fantazzk.template.TemplateId;
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
    public Room create(TemplateId templateId, String hostNickname) {
        TemplateCatalog.TemplateBlueprint template = getTemplate(templateId);

        Room room =
            Room.createFromTemplate(
                generateCode(),
                UUID.randomUUID().toString(),
                hostNickname,
                new RoomTemplateSpec(
                    template.mode() == com.naminhyeok.fantazzk.template.TemplateMode.AUCTION
                        ? RoomTemplateSpec.Mode.AUCTION
                        : RoomTemplateSpec.Mode.DRAFT,
                    template.teamCount(),
                    template.teamSize(),
                    template.budget(),
                    template.draftOrderStrategy() == null ? null : RoomTemplateSpec.DraftOrderStrategy.valueOf(template.draftOrderStrategy().name()),
                    template.players().stream()
                        .map(player -> new RoomTemplateSpec.Player(player.name(), player.displayOrder()))
                        .toList()
                )
            );

        return rooms.save(room);
    }

    private TemplateCatalog.TemplateBlueprint getTemplate(TemplateId templateId) {
        try {
            return templateCatalog.getTemplate(templateId);
        } catch (TemplateCatalogException.NotFound ex) {
            throw new RoomTemplateNotFoundException();
        }
    }

    private String generateCode() {
        return UUID.randomUUID().toString().replace("-", "").substring(0, 6).toUpperCase();
    }
}
