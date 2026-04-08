package com.naminhyeok.fantazzk.template;

import java.util.List;
import java.util.UUID;

public interface TemplateManagement {
    TemplateSummaryView create(CreateTemplateInput input);

    TemplateDetailView getDetail(UUID templateId);

    List<TemplateSummaryView> list();
}
