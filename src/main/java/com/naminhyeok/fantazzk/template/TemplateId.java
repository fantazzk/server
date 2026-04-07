package com.naminhyeok.fantazzk.template;

import java.util.UUID;
import org.jmolecules.ddd.types.Identifier;

public record TemplateId(UUID templateId) implements Identifier {
}
