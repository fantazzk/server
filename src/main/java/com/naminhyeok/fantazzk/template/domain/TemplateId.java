package com.naminhyeok.fantazzk.template.domain;

import java.util.UUID;
import org.jmolecules.ddd.types.Identifier;

public record TemplateId(UUID templateId) implements Identifier {
}
