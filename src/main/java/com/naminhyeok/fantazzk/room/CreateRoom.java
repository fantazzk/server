package com.naminhyeok.fantazzk.room;

import com.naminhyeok.fantazzk.auction.AuctionPlayerSeed;
import com.naminhyeok.fantazzk.auction.AuctionRoomLifecycle;
import com.naminhyeok.fantazzk.auction.AuctionRoomSetup;
import com.naminhyeok.fantazzk.CoreException;
import com.naminhyeok.fantazzk.draft.DraftOrderStrategy;
import com.naminhyeok.fantazzk.draft.DraftPlayerSpec;
import com.naminhyeok.fantazzk.draft.DraftRoomLifecycle;
import com.naminhyeok.fantazzk.template.TemplateCatalog;
import java.time.Clock;
import java.time.Instant;
import java.util.UUID;
import java.util.regex.Pattern;
import org.hibernate.exception.ConstraintViolationException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Component;

@Component
@org.jmolecules.ddd.annotation.Service
class CreateRoom {
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
    private final DraftRoomLifecycle draftRoomLifecycle;
    private final AuctionRoomLifecycle auctionRoomLifecycle;

    CreateRoom(
        CreateRoomAttempt createRoomAttempt,
        TemplateCatalog templateCatalog,
        TeamLeaderIdentityIssuer teamLeaderIdentityIssuer,
        Clock clock,
        RoomCodeGenerator roomCodeGenerator,
        DraftRoomLifecycle draftRoomLifecycle,
        AuctionRoomLifecycle auctionRoomLifecycle
    ) {
        this.createRoomAttempt = createRoomAttempt;
        this.templateCatalog = templateCatalog;
        this.teamLeaderIdentityIssuer = teamLeaderIdentityIssuer;
        this.clock = clock;
        this.roomCodeGenerator = roomCodeGenerator;
        this.draftRoomLifecycle = draftRoomLifecycle;
        this.auctionRoomLifecycle = auctionRoomLifecycle;
    }

    public RoomSessionResult create(UUID templateId, String hostNickname) {
        TemplateCatalog.TemplateBlueprint template = getTemplate(templateId);
        TeamLeaderIdentityIssuer.TeamLeaderIdentity identity = teamLeaderIdentityIssuer.issue();
        TeamLeaderId hostLeaderId = new TeamLeaderId(identity.leaderId());

        for (int attempt = 1; attempt <= MAX_ROOM_CODE_ATTEMPTS; attempt++) {
            Room room = newRoom(template, hostLeaderId, identity.actionToken(), hostNickname);
            try {
                Room saved = createRoomAttempt.save(room, savedRoom -> createGameplay(savedRoom, template, hostLeaderId, hostNickname));
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
        return Room.createFromTemplate(
            generateCode(),
            hostLeaderId,
            hostNickname,
            hostActionToken,
            new RoomTemplateSpec(
                template.mode() == TemplateCatalog.Mode.AUCTION
                    ? RoomTemplateSpec.Mode.AUCTION
                    : RoomTemplateSpec.Mode.DRAFT,
                template.teamCount(),
                template.teamSize(),
                template.budget(),
                template.pickBanTime(),
                template.minBidUnit(),
                template.positionLimit(),
                template.draftOrderStrategy() == null
                    ? null
                    : RoomTemplateSpec.DraftOrderStrategy.valueOf(template.draftOrderStrategy().name()),
                template.players().stream()
                    .map(player -> new RoomTemplateSpec.Player(
                        new RoomPlayerId(player.playerIndex()),
                        player.name(),
                        player.position(),
                        player.playerIndex()
                    ))
                    .toList()
            ),
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

    private void createGameplay(
        Room room,
        TemplateCatalog.TemplateBlueprint template,
        TeamLeaderId hostLeaderId,
        String hostNickname
    ) {
        if (draftRoomLifecycle == null || auctionRoomLifecycle == null) {
            return;
        }
        if (room.getMode() == RoomMode.DRAFT) {
            draftRoomLifecycle.create(
                room.getCode(),
                template.teamCount(),
                template.teamSize(),
                DraftOrderStrategy.valueOf(template.draftOrderStrategy().name()),
                template.players().stream()
                    .map(player -> new DraftPlayerSpec(player.playerIndex(), player.name(), player.position(), player.playerIndex()))
                    .toList()
            );
            draftRoomLifecycle.addLeader(room.getCode(), hostLeaderId.value(), hostNickname);
            return;
        }

        auctionRoomLifecycle.create(
            room.getCode(),
            hostLeaderId.value(),
            hostNickname,
            room.getCreatedAt(),
            new AuctionRoomSetup(
                template.teamCount(),
                template.teamSize(),
                template.budget(),
                template.pickBanTime(),
                template.minBidUnit(),
                template.positionLimit(),
                template.players().stream()
                    .map(player -> new AuctionPlayerSeed(player.playerIndex(), player.name(), player.position(), player.playerIndex()))
                    .toList()
            )
        );
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
