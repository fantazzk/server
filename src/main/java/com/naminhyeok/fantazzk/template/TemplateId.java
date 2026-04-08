package com.naminhyeok.fantazzk.template;

import java.util.UUID;
import org.jmolecules.ddd.types.Identifier;

record TemplateId(UUID templateId) implements Identifier {
}
