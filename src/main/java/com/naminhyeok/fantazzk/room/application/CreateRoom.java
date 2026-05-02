package com.naminhyeok.fantazzk.room.application;

import com.naminhyeok.fantazzk.CoreException;
import com.naminhyeok.fantazzk.room.domain.Room;
import com.naminhyeok.fantazzk.room.domain.RoomErrorType;
import com.naminhyeok.fantazzk.room.domain.RoomTeamLeader;
import com.naminhyeok.fantazzk.room.domain.RoomTemplateSpec;
import com.naminhyeok.fantazzk.room.domain.TeamLeaderId;
import com.naminhyeok.fantazzk.template.TemplateCatalog;
import java.time.Clock;
import java.time.Instant;
import java.util.UUID;
import java.util.regex.Pattern;
import lombok.RequiredArgsConstructor;
import org.hibernate.exception.ConstraintViolationException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CreateRoom {
    private static final int MAX_ROOM_CODE_ATTEMPTS = 3;
    private static final String ROOM_CODE_CONSTRAINT = "uk_rooms_code";
    private static final Pattern ROOM_CODE_CONSTRAINT_PATTERN = Pattern.compile(
        "(?i)(^|[^a-z0-9_])" + ROOM_CODE_CONSTRAINT + "([^a-z0-9_]|$)"
    );

    private final CreateRoomAttempt createRoomAttempt;
    private final TemplateCatalog templateCatalog;
    private final Clock clock;
    private final RoomCodeGenerator roomCodeGenerator;

    public RoomSessionResult create(UUID templateId, String hostNickname) {
        TemplateCatalog.TemplateBlueprint template = getTemplate(templateId);
        TeamLeaderId hostLeaderId = new TeamLeaderId(UUID.randomUUID().toString());
        String hostActionToken = UUID.randomUUID().toString();

        for (int attempt = 1; attempt <= MAX_ROOM_CODE_ATTEMPTS; attempt++) {
            Room room = newRoom(template, hostLeaderId, hostActionToken, hostNickname);
            RoomTeamLeader hostLeader = room.getLeaders().getFirst();
            try {
                Room saved = createRoomAttempt.save(room);
                return new RoomSessionResult(saved, hostLeader);
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
        TeamLeaderId hostLeaderId,
        String hostActionToken,
        String hostNickname
    ) {
        RoomTemplateSpec spec = RoomTemplateSpecFactory.from(template);
        return Room.createFromTemplate(
            generateCode(),
            hostLeaderId,
            hostNickname,
            hostActionToken,
            spec,
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
                isRoomCodeConstraint(constraintViolationException.getConstraintName())
            ) {
                return true;
            }
            current = current.getCause();
        }
        return false;
    }

    private boolean isRoomCodeConstraint(String constraintName) {
        return constraintName != null && ROOM_CODE_CONSTRAINT_PATTERN.matcher(constraintName).find();
    }
}
