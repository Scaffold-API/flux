/*
 * Copyright Scaffold Software LLC. All Rights Reserved.
 * SPDX-License-Identifier: Apache-2.0
 */
package com.scaffold.api.plugins.language;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import org.languagetool.JLanguageTool;
import org.languagetool.rules.spelling.SpellingCheckRule;
import software.amazon.smithy.model.Model;
import software.amazon.smithy.model.knowledge.TextIndex;
import software.amazon.smithy.model.knowledge.TextInstance;
import software.amazon.smithy.model.node.NodeMapper;
import software.amazon.smithy.model.traits.DocumentationTrait;
import software.amazon.smithy.model.validation.AbstractValidator;
import software.amazon.smithy.model.validation.ValidationEvent;
import software.amazon.smithy.model.validation.ValidatorService;
import software.amazon.smithy.utils.SmithyInternalApi;

@SmithyInternalApi
public final class ProofreadValidator extends AbstractValidator {
    private final JLanguageTool tool;

    public static final class Config {
        private String language = "en";

        public void setLanguage(String language) {
            this.language = language;
        }
    }

    // Maps metadata from the model into the validator config and instatiates Linter
    public static final class Provider extends ValidatorService.Provider {
        public Provider() {
            super(ProofreadValidator.class, configuration -> {
                var mapper = new NodeMapper();
                var cfg = mapper.deserialize(configuration, Config.class);
                return new ProofreadValidator(cfg);
            });
        }
    }

    private ProofreadValidator(Config config) {
        var lang = Objects.requireNonNullElse(config.language, "en");
        this.tool = LanguageToolService.expect(lang);
        // Disable spellcheck rules.
        for (var rule : tool.getAllActiveRules()) {
            if (rule instanceof SpellingCheckRule) {
                tool.disableRule(rule.getId());
            }
        }
    }

    @Override
    public List<ValidationEvent> validate(Model model) {
        List<ValidationEvent> results = new ArrayList<>();
        var textIndex = TextIndex.of(model);
        for (var text : textIndex.getTextInstances()) {
            // Only check grammar in docstrings
            if (text.getLocationType().equals(TextInstance.TextLocationType.APPLIED_TRAIT)
                    && text.getTrait().toShapeId().equals(DocumentationTrait.ID)) {
                var docTrait = text.getTrait();
                var matches = LanguageCheckingUtils.getMatches(this.tool, text.getText());
                for (var match : matches) {
                    String message = match.getMessage()
                            .replace("<suggestion>", "`")
                            .replace("</suggestion>", "`");
                    results.add(
                            danger(
                                    text.getShape(),
                                    docTrait.getSourceLocation(),
                                    message));
                }
            }
        }
        return results;
    }
}
