/*
 * Copyright Scaffold Software LLC. All Rights Reserved.
 * SPDX-License-Identifier: Apache-2.0
 */
package com.scaffold.api.plugins.language;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import org.languagetool.JLanguageTool;
import org.languagetool.rules.RuleMatch;
import software.amazon.smithy.model.Model;
import software.amazon.smithy.model.SourceLocation;
import software.amazon.smithy.model.knowledge.TextIndex;
import software.amazon.smithy.model.knowledge.TextInstance;
import software.amazon.smithy.model.node.NodeMapper;
import software.amazon.smithy.model.shapes.Shape;
import software.amazon.smithy.model.traits.DocumentationTrait;
import software.amazon.smithy.model.traits.Trait;
import software.amazon.smithy.model.validation.AbstractValidator;
import software.amazon.smithy.model.validation.Severity;
import software.amazon.smithy.model.validation.ValidationEvent;
import software.amazon.smithy.model.validation.ValidatorService;
import software.amazon.smithy.utils.SmithyInternalApi;
import software.amazon.smithy.utils.StringUtils;

/**
 * TODO: DOCS
 */
@SmithyInternalApi
public final class SpellCheckValidator extends AbstractValidator {
    private static final String TRAIT = "Trait";
    private static final String SHAPE = "Shape";
    private static final String NAMESPACE = "Namespace";
    private static final Set<Character> TRIGGER_CHARS = Set.of('-', '_');

    private final JLanguageTool tool;
    private final Config config;

    public static final class Config {
        private List<String> ignore = Collections.emptyList();
        private boolean docstrings = true;
        private int limit = 4;
        private String language = "en";

        public void setIgnore(List<String> ignore) {
            this.ignore = Objects.requireNonNull(ignore);
        }

        public void setDocstrings(boolean check) {
            this.docstrings = check;
        }

        public void setLimit(int limit) {
            this.limit = limit;
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
                var cfg = mapper.deserialize(configuration, Config.class);
                return new SpellCheckValidator(cfg);
            });
        }
    }

    private SpellCheckValidator(Config config) {
        var lang = Objects.requireNonNullElse(config.language, "en");
        this.tool = LanguageToolService.expect(lang);
        LanguageCheckingUtils.configureSpellcheck(this.tool, config.ignore);
        this.config = config;
    }

    @Override
    public List<ValidationEvent> validate(Model model) {
        List<ValidationEvent> results = new ArrayList<>();
        var textIndex = TextIndex.of(model);
        for (var text : textIndex.getTextInstances()) {
            for (var match : LanguageCheckingUtils.getMatches(this.tool, text.getText())) {
                var event = typoEvent(text, match, this.config.docstrings);
                if (event != null) {
                    results.add(event);
                }
            }
        }
        return results;
    }

    private ValidationEvent typoEvent(TextInstance text, RuleMatch match, boolean checkDocstrings) {
        return switch (text.getLocationType()) {
            case APPLIED_TRAIT -> {
                if (text.getTrait().toShapeId().equals(DocumentationTrait.ID) && !checkDocstrings) {
                    // Ignore typos in docstrings
                    yield null;
                }
                ValidationEvent validationEvent = danger(text.getShape(), text.getTrait().getSourceLocation(), "");
                String idiomaticTraitName = Trait.getIdiomaticTraitName(text.getTrait());
                List<String> suggestions = computeSuggestions(text.getText(), match, this.config.limit);
                if (text.getTrait().toShapeId().equals(DocumentationTrait.ID)) {
                    yield validationEvent.toBuilder()
                            .id(getName() + "." + TRAIT + "." + idiomaticTraitName)
                            .message(String.format(
                                    "Potential typo in docstring. Suggested correction(s): %s",
                                    suggestions))
                            .build();
                } else if (text.getTraitPropertyPath().isEmpty()) {
                    yield validationEvent.toBuilder()
                            .id(getName() + "." + TRAIT + "." + idiomaticTraitName)
                            .message(String.format(
                                    "Potential typo in trait `%s`. Suggested correction(s): %s",
                                    idiomaticTraitName,
                                    suggestions))
                            .build();
                } else {
                    String propertyPath = String.join(".", text.getTraitPropertyPath());
                    yield validationEvent.toBuilder()
                            .id(getName() + "." + TRAIT + "." + idiomaticTraitName + "." + propertyPath)
                            .message(String.format(
                                    "Potential typo in trait `%s` at path {%s}. Suggested correction(s): %s",
                                    idiomaticTraitName,
                                    propertyPath,
                                    suggestions))
                            .build();
                }
            }
            case NAMESPACE -> {
                var suggestions = computeSuggestions(text.getText(), match, this.config.limit).stream()
                        .map(StringUtils::lowerCase)
                        .toList();
                yield ValidationEvent.builder()
                        .severity(Severity.DANGER)
                        .sourceLocation(SourceLocation.none())
                        .id(getName() + "." + NAMESPACE)
                        .message(String.format("Potential typo in namespace `%s`. Suggested correction(s): %s",
                                text.getText(),
                                suggestions))
                        .build();
            }
            default -> danger(text.getShape(),
                    String.format(
                            "Potential typo in shape name `%s`. Suggested correction(s): %s",
                            getShapeName(text.getShape()),
                            computeSuggestions(text.getText(), match, this.config.limit)),
                    SHAPE);
        };
    }

    private static List<String> computeSuggestions(String previous, RuleMatch match, int limit) {
        var builder = new StringBuilder();
        if (match.getFromPos() != 0) {
            builder.append(previous, 0, match.getFromPos());
        }
        builder.append("%s");
        if (match.getToPos() != previous.length()) {
            builder.append(previous, match.getToPos(), previous.length() - 1);
        }
        var template = builder.toString();
        return match.getSuggestedReplacements()
                .stream()
                .map(String::toLowerCase)
                .distinct()
                .limit(limit)
                .map(s -> computeSuggestion(template, s))
                .toList();
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
