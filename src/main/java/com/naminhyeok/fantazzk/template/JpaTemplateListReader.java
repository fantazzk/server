package com.naminhyeok.fantazzk.template;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
class JpaTemplateListReader implements TemplateListReader {
    private final TemplateListJpaRepository repository;

    @Override
    public List<TemplateDetail> list() {
        return repository.findAll().stream()
            .map(template -> new TemplateDetail(template, template.getPlayers()))
            .toList();
    }
}
