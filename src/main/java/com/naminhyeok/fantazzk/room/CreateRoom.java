package com.naminhyeok.fantazzk.room;

import com.naminhyeok.fantazzk.CoreException;
import com.naminhyeok.fantazzk.template.TemplateCatalog;
import java.time.Clock;
import java.time.Instant;
import java.util.UUID;
import org.hibernate.exception.ConstraintViolationException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
class CreateRoom {
    private static final int MAX_ROOM_CODE_ATTEMPTS = 3;
    private static final String ROOM_CODE_CONSTRAINT = "uk_rooms_code";

    private final CreateRoomAttempt createRoomAttempt;
    private final TemplateCatalog templateCatalog;
    private final TeamLeaderIdentityIssuer teamLeaderIdentityIssuer;
    private final Clock clock;
    private final RoomCodeGenerator roomCodeGenerator;

    public Room create(UUID templateId, String hostNickname) {
        TemplateCatalog.TemplateBlueprint template = getTemplate(templateId);
        TeamLeaderIdentityIssuer.TeamLeaderIdentity identity = teamLeaderIdentityIssuer.issue();

        for (int attempt = 1; attempt <= MAX_ROOM_CODE_ATTEMPTS; attempt++) {
            Room room = newRoom(template, identity, hostNickname);
            try {
                return createRoomAttempt.save(room);
            } catch (DataIntegrityViolationException ex) {
                if (!isRoomCodeCollision(ex)) {
                    throw ex;
                }
                if (attempt == MAX_ROOM_CODE_ATTEMPTS) {
                    throw CoreException.of(RoomErrorType.ROOM_CODE_GENERATION_FAILED);
                }
            }
        }

        throw CoreException.of(RoomErrorType.ROOM_CODE_GENERATION_FAILED);
    }

    private TemplateCatalog.TemplateBlueprint getTemplate(UUID templateId) {
        try {
            return templateCatalog.getTemplate(templateId);
        } catch (TemplateCatalog.NotFound ex) {
            throw CoreException.of(RoomErrorType.ROOM_TEMPLATE_NOT_FOUND);
        }
    }

    private Room newRoom(
        TemplateCatalog.TemplateBlueprint template,
        TeamLeaderIdentityIssuer.TeamLeaderIdentity identity,
        String hostNickname
    ) {
        return Room.createFromTemplate(
            generateCode(),
            identity.leaderId(),
            hostNickname,
            identity.actionToken(),
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
                    .map(player -> new RoomTemplateSpec.Player(
                        new RoomPlayerId(player.playerIndex()),
                        player.name(),
                        player.playerIndex()
                    ))
                    .toList()
            ),
            Instant.now(clock)
        );
    }

    private String generateCode() {
        return roomCodeGenerator.generate();
    }

    private boolean isRoomCodeCollision(Throwable throwable) {
        Throwable current = throwable;
        while (current != null) {
            if (
                current instanceof ConstraintViolationException constraintViolationException &&
                constraintViolationException.getConstraintName() != null &&
                ROOM_CODE_CONSTRAINT.equalsIgnoreCase(constraintViolationException.getConstraintName())
            ) {
                return true;
            }
            current = current.getCause();
        }
        return false;
    }
}
