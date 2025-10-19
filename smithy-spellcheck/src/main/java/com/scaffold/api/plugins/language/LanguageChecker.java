/*
 * Copyright Scaffold Software LLC. All Rights Reserved.
 * SPDX-License-Identifier: Apache-2.0
 */
package com.scaffold.api.plugins.language;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.List;
import java.util.function.Supplier;
import org.languagetool.JLanguageTool;
import org.languagetool.Language;
import org.languagetool.markup.AnnotatedText;
import org.languagetool.rules.RuleMatch;
import org.languagetool.rules.spelling.SpellingCheckRule;

public final class LanguageChecker {
    private final JLanguageTool language;

    public LanguageChecker(Language language) {
        this.language = new JLanguageTool(language);
    }

    /**
     * Disables all non-spellcheck rules.
     *
     * @param ignored - Tokens to ignore when spell checking
     */
    public void spellCheckOnly(List<String> ignored) {
        for (var rule : this.language.getAllActiveRules()) {
            if (rule instanceof SpellingCheckRule scr) {
                scr.addIgnoreTokens(ignored);
            } else {
                // Deactivate any non-spellcheck rules
                this.language.disableRule(rule.getId());
            }
        }
    }

    public List<RuleMatch> getMatches(String text) {
        return getMatches(AnnotationUtils.annotateText(text));
    }

    private List<RuleMatch> getMatches(AnnotatedText text) {
        try {
            return language.check(text);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public abstract static class Provider implements LanguageCheckerService {
        private final String languageCode;
        private final Supplier<Language> supplier;

        protected Provider(String languageCode, Supplier<Language> supplier) {
            this.languageCode = languageCode;
            this.supplier = supplier;
        }

        @Override
        public String getLanguageCode() {
            return this.languageCode;
        }

        public LanguageChecker create() {
            return new LanguageChecker(this.supplier.get());
        }
    }
}
