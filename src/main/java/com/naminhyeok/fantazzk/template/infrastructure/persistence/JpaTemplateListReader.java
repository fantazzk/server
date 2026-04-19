package com.naminhyeok.fantazzk.template.infrastructure.persistence;

import com.naminhyeok.fantazzk.template.query.TemplateDetail;
import com.naminhyeok.fantazzk.template.query.TemplateListReader;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class JpaTemplateListReader implements TemplateListReader {
    private final TemplateListJpaRepository repository;

    @Override
    public List<TemplateDetail> list() {
        return repository.findAll().stream()
            .map(template -> new TemplateDetail(template, template.getPlayers()))
            .toList();
    }
}
