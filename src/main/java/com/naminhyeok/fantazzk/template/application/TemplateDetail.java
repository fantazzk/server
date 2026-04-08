package com.naminhyeok.fantazzk.template.application;

import com.naminhyeok.fantazzk.template.domain.Template;
import com.naminhyeok.fantazzk.template.domain.TemplatePlayer;
import java.util.List;

public record TemplateDetail(Template template, List<TemplatePlayer> players) {
}
