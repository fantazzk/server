package com.naminhyeok.fantazzk.template.domain;

import com.naminhyeok.fantazzk.template.TemplateId;
import jakarta.persistence.Column;
import jakarta.persistence.FetchType;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.Objects;
import org.springframework.lang.Nullable;

@jakarta.persistence.Entity
@Table(name = "template_player")
public class TemplatePlayer implements org.jmolecules.ddd.types.Entity<Template, TemplatePlayerId> {

    @EmbeddedId
    private TemplatePlayerId id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "template_id", nullable = false)
    private Template template;

    @Column(name = "name", nullable = false)
    private String name = "";

    @Column(name = "display_order", nullable = false)
    private int displayOrder = 0;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt = Instant.now();

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt = Instant.now();

    protected TemplatePlayer() {}

    public TemplatePlayer(
            TemplatePlayerId templatePlayerId,
            TemplateId templateId,
            String name,
            int displayOrder,
            Instant createdAt,
            Instant updatedAt
    ) {
        this.id = templatePlayerId == null ? TemplatePlayerId.newId() : templatePlayerId;
        this.template = Template.reference(templateId);
        this.name = name;
        this.displayOrder = displayOrder;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public TemplatePlayer(
            TemplateId templateId,
            String name,
            int displayOrder,
            Instant createdAt,
            Instant updatedAt
    ) {
        this(null, templateId, name, displayOrder, createdAt, updatedAt);
    }

    public TemplatePlayer(
            TemplatePlayerId templatePlayerId,
            TemplateId templateId,
            String name,
            int displayOrder
    ) {
        this(templatePlayerId, templateId, name, displayOrder, Instant.now(), Instant.now());
    }

    public TemplatePlayer(TemplateId templateId, String name, int displayOrder) {
        this(templateId, name, displayOrder, Instant.now(), Instant.now());
    }

    public TemplatePlayerId getId() {
        return id;
    }

    public Template getTemplate() {
        return template;
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

    @Nullable
    public TemplateId getTemplateId() {
        return template == null ? null : template.getId();
    }

    boolean belongsTo(TemplateId templateId) {
        return Objects.equals(getTemplateId(), templateId);
    }

    TemplatePlayer attach(Template template) {
        this.template = template;
        return this;
    }
}
