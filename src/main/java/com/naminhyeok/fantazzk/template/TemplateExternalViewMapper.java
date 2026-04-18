package com.naminhyeok.fantazzk.template;

final class TemplateExternalViewMapper {
    private TemplateExternalViewMapper() {
    }

    static TemplateCatalog.TemplateBlueprint toBlueprint(TemplateDetail detail) {
        Template template = detail.template();
        return new TemplateCatalog.TemplateBlueprint(
            template.getMode(),
            template.getTeamCount(),
            template.getTeamSize(),
            template.getBudget(),
            template.getPickBanTime(),
            template.getMinBidUnit(),
            template.getPositionLimit(),
            template.getDraftOrderStrategy(),
            detail.players().stream()
                .map(player -> new TemplateCatalog.PlayerBlueprint(player.name(), player.position(), player.displayOrder()))
                .toList()
        );
    }

    static TemplateResponse toResponse(TemplateDetail detail) {
        Template template = detail.template();
        return new TemplateResponse(
            template.getId().templateId().toString(),
            template.getName(),
            TemplateCatalog.GameType.valueOf(template.getGameType().name()),
            template.getMode(),
            template.getTeamCount(),
            template.getTeamSize(),
            template.getBudget(),
            template.getPickBanTime(),
            template.getMinBidUnit(),
            template.getPositionLimit(),
            template.getDraftOrderStrategy(),
            detail.players().stream().map(TemplatePlayerResponse::from).toList()
        );
    }
}
