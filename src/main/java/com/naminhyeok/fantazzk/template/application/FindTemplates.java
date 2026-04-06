package com.naminhyeok.fantazzk.template.application;

import com.naminhyeok.fantazzk.template.TemplateId;
import com.naminhyeok.fantazzk.template.domain.Template;
import com.naminhyeok.fantazzk.template.domain.TemplatePlayer;
import com.naminhyeok.fantazzk.template.exception.TemplateException;
import com.naminhyeok.fantazzk.template.repository.Templates;
import java.util.List;
import org.springframework.dao.InvalidDataAccessApiUsageException;
import org.springframework.transaction.annotation.Transactional;

@org.jmolecules.ddd.annotation.Service
@org.springframework.stereotype.Service
public class FindTemplates {
    private final Templates templateRepository;

    public FindTemplates(Templates templateRepository) {
        this.templateRepository = templateRepository;
    }

    @Transactional(readOnly = true)
    public TemplateDetail getDetail(TemplateId templateId) {
        try {
            Template template = templateRepository.findById(templateId);
            if (template == null) {
                throw new TemplateException.TemplateNotFoundException();
            }

            List<TemplatePlayer> players = template.players();
            template.requireValidRoster(players);
            return new TemplateDetail(template, players);
        } catch (IllegalArgumentException e) {
            throw new TemplateException.TemplateInvalidException();
        } catch (InvalidDataAccessApiUsageException e) {
            throw new TemplateException.TemplateInvalidException();
        }
    }

    @Transactional(readOnly = true)
    public List<Template> list() {
        try {
            List<Template> templates = templateRepository.findAll();
            for (Template template : templates) {
                template.players();
            }
            return templates;
        } catch (IllegalArgumentException e) {
            throw new TemplateException.TemplateInvalidException();
        } catch (InvalidDataAccessApiUsageException e) {
            throw new TemplateException.TemplateInvalidException();
        }
    }
}
