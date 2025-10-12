/*
 * Copyright Scaffold Software LLC. All Rights Reserved.
 * SPDX-License-Identifier: Apache-2.0
 */
package com.scaffold.api.plugins.spellcheck;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import org.languagetool.JLanguageTool;
import org.languagetool.Languages;
import org.languagetool.rules.Categories;
import org.languagetool.rules.RuleMatch;
import org.languagetool.rules.TestRemoteRule;
import org.languagetool.rules.spelling.SpellingCheckRule;
import software.amazon.smithy.model.Model;
import software.amazon.smithy.model.node.NodeMapper;
import software.amazon.smithy.model.shapes.Shape;
import software.amazon.smithy.model.validation.AbstractValidator;
import software.amazon.smithy.model.validation.Severity;
import software.amazon.smithy.model.validation.ValidationEvent;
import software.amazon.smithy.model.validation.ValidatorService;
import software.amazon.smithy.utils.SmithyInternalApi;
import org.languagetool.rules.en.MorfologikAmericanSpellerRule;
import software.amazon.smithy.utils.StringUtils;

/**
 * TODO: DOCS
 */
@SmithyInternalApi
public final class SpellCheckValidator extends AbstractValidator {
    private static final String ROOT_ID = "spellcheck.typo";
    private static final String NAME_TYPO_ID = String.format("%s.name", ROOT_ID);
    private static final String DOCUMENTATION_TYPO_ID = String.format("%s.documentation", ROOT_ID);
    private static final Set<Character> TRIGGER_CHARS = Set.of('-', '_');

    private final JLanguageTool checker;

    public static final class Config {
        private List<String> ignore = Collections.emptyList();
        private String language = "en";

        public List<String> getIgnore() {
            return ignore;
        }

        public String getLanguage() {
            return language;
        }

        public void setIgnore(List<String> ignore) {
            this.ignore = ignore;
        }

        public void setLanguage(String language) {
            this.language = Objects.requireNonNull(language);
        }
    }

    // Maps metadata from the model into the validator config and instatiates Linter
    public static final class Provider extends ValidatorService.Provider {
        public Provider() {
            super(SpellCheckValidator.class, configuration -> {
                var mapper = new NodeMapper();
                var config = mapper.deserialize(configuration, Config.class);
                return new SpellCheckValidator(config);
            });
        }
    }

    private SpellCheckValidator(Config config) {
        //var lang = Objects.requireNonNullElse(config.language, "en-GB");
        var lt = new JLanguageTool(new CodingEnglish());
        // Deactivate any non-spellcheck rules
        for (var rule: lt.getAllActiveRules()) {
            if (!rule.isDictionaryBasedSpellingRule()) {
                lt.disableRule(rule.getId());
            }
            // TODO: Add ignore tokens
        };
        this.checker = lt;
    }

    @Override
    public List<ValidationEvent> validate(Model model) {
        List<ValidationEvent> results = new ArrayList<>();
        results.addAll(checkShapeNames(model));
        return results;
    }

    /**
     * Checks all shape names (including member names) for potential typos.
     */
    private List<ValidationEvent> checkShapeNames(final Model model) {
        List<ValidationEvent> results = new ArrayList<>();
        for (var shape: model.shapes().filter(s -> !s.getId().getNamespace().startsWith("smithy")).toList()) {
            for (var match: getMatches(getShapeName(shape))) {
                results.add(nameTypo(shape, match));
            }
        }
        return results;
    }

    private List<RuleMatch> getMatches(String text) {
        try {
            return this.checker.check(text);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private static ValidationEvent nameTypo(Shape shape, RuleMatch match) {
        var shapeName = getShapeName(shape);
        var builder = new StringBuilder();
        if (match.getFromPos() != 0) {
            builder.append(shapeName, 0, match.getFromPos());
        }
        builder.append("%s");
        if (match.getToPos() != shapeName.length()) {
            builder.append(shapeName, match.getToPos(), shapeName.length() - 1);
        }
        var template = builder.toString();
        System.out.println("TEMPLATE: " + template);
        var suggestions = match.getSuggestedReplacements().stream()
                .map(String::toLowerCase)
                .distinct()
                .limit(4)
                .map(s -> computeSuggestion(template, s))
                .toList();
        var message = String.format(
                "Potential typo in shape name `%s`. Suggested correction(s): %s",
                shapeName,
                suggestions
        );
        return ValidationEvent.builder()
                .id(NAME_TYPO_ID)
                .severity(Severity.DANGER)
                .sourceLocation(shape)
                .shapeId(shape)
                .message(message)
                .build();
    }

    private static String computeSuggestion(String template, String rec) {
        // If snake or camel case, keep lower case in the recommendation.
        var index = template.indexOf("%s");
        var previous = index == 0 ? null : template.charAt(index - 1);
        if (previous != null && TRIGGER_CHARS.contains(previous)) {
            return String.format(template, StringUtils.lowerCase(rec));
        }
        return String.format(template, StringUtils.capitalize(rec));
    }

    private static String getShapeName(Shape shape) {
        if (shape.isMemberShape()) {
            return shape.getId().getMember().orElseThrow(() -> new RuntimeException("Expected member."));
        }
        return shape.getId().getName();
    }
}
