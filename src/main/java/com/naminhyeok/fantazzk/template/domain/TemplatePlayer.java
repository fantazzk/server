package com.naminhyeok.fantazzk.template.domain;

import com.naminhyeok.fantazzk.template.TemplateId;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;
import org.springframework.lang.Nullable;

@Entity
@Table(name = "template_player")
public class TemplatePlayer {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id")
    private UUID persistentId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "template_id", nullable = false)
    private Template template;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "display_order", nullable = false)
    private int displayOrder;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    protected TemplatePlayer() {
        this((UUID) null, (Template) null, "", 0, Instant.now(), Instant.now());
    }

    public TemplatePlayer(String name, int displayOrder) {
        this((UUID) null, (Template) null, name, displayOrder, Instant.now(), Instant.now());
    }

    public TemplatePlayer(
        @Nullable
        TemplatePlayerId templatePlayerId,
        @Nullable
        TemplateId templateId,
        String name,
        int displayOrder,
        Instant createdAt,
        Instant updatedAt
    ) {
        this(
            templatePlayerId == null ? null : templatePlayerId.getValue(),
            templateId == null ? null : Template.reference(templateId),
            name,
            displayOrder,
            createdAt,
            updatedAt
        );
    }

    public TemplatePlayer(
        @Nullable
        TemplateId templateId,
        String name,
        int displayOrder,
        Instant createdAt,
        Instant updatedAt
    ) {
        this(null, templateId, name, displayOrder, createdAt, updatedAt);
    }

    public TemplatePlayer(@Nullable TemplateId templateId, String name, int displayOrder) {
        this(templateId, name, displayOrder, Instant.now(), Instant.now());
    }

    private TemplatePlayer(
        UUID persistentId,
        Template template,
        String name,
        int displayOrder,
        Instant createdAt,
        Instant updatedAt
    ) {
        this.persistentId = persistentId;
        this.template = template;
        this.name = Objects.requireNonNull(name, "name must not be null");
        this.displayOrder = displayOrder;
        this.createdAt = Objects.requireNonNull(createdAt, "createdAt must not be null");
        this.updatedAt = Objects.requireNonNull(updatedAt, "updatedAt must not be null");
    }

    @Nullable
    public TemplatePlayerId getTemplatePlayerId() {
        return persistentId == null ? null : new TemplatePlayerId(persistentId);
    }

    @Nullable
    public TemplateId getTemplateId() {
        return template == null ? null : template.getTemplateId();
    }

    public String getName() {
        return name;
    }

    public int getDisplayOrder() {
        return displayOrder;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    void attach(Template template) {
        this.template = Objects.requireNonNull(template, "template must not be null");
    }
}
