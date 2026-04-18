package com.naminhyeok.fantazzk.room.application.room;

import com.naminhyeok.fantazzk.room.domain.game.*;
import com.naminhyeok.fantazzk.room.domain.handoff.*;
import com.naminhyeok.fantazzk.room.domain.repository.*;
import com.naminhyeok.fantazzk.room.domain.room.*;
import com.naminhyeok.fantazzk.room.domain.shared.*;

import com.naminhyeok.fantazzk.CoreException;
import com.naminhyeok.fantazzk.room.application.port.RoomCodeGenerator;
import com.naminhyeok.fantazzk.room.application.port.TeamLeaderIdentityIssuer;
import com.naminhyeok.fantazzk.room.application.support.CreateRoomAttempt;
import com.naminhyeok.fantazzk.template.TemplateCatalog;
import java.time.Clock;
import java.time.Instant;
import java.util.UUID;
import java.util.regex.Pattern;
import org.hibernate.exception.ConstraintViolationException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import lombok.RequiredArgsConstructor;

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
    private final TeamLeaderIdentityIssuer teamLeaderIdentityIssuer;
    private final Clock clock;
    private final RoomCodeGenerator roomCodeGenerator;

    public RoomSessionResult create(UUID templateId, String hostNickname) {
        TemplateCatalog.TemplateBlueprint template = getTemplate(templateId);
        TeamLeaderIdentityIssuer.TeamLeaderIdentity identity = teamLeaderIdentityIssuer.issue();
        TeamLeaderId hostLeaderId = new TeamLeaderId(identity.leaderId());

        for (int attempt = 1; attempt <= MAX_ROOM_CODE_ATTEMPTS; attempt++) {
            Room room = newRoom(template, hostLeaderId, identity.actionToken(), hostNickname);
            try {
                Room saved = createRoomAttempt.save(room);
                return new RoomSessionResult(saved, findLeader(saved, hostLeaderId));
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
        RoomTemplateSpec spec = RoomTemplateSpec.from(template);
        return Room.createFromTemplate(
            generateCode(),
            hostLeaderId,
            hostNickname,
            hostActionToken,
            spec,
            Instant.now(clock)
        );
    }

    private RoomTeamLeader findLeader(Room room, TeamLeaderId leaderId) {
        return room.getLeaders().stream()
            .filter(leader -> leader.getId().equals(leaderId))
            .findFirst()
            .orElseThrow();
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
