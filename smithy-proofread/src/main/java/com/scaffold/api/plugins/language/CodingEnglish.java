/*
 * Copyright Scaffold Software LLC. All Rights Reserved.
 * SPDX-License-Identifier: Apache-2.0
 */
package com.scaffold.api.plugins.language;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import org.languagetool.Language;
import org.languagetool.UserConfig;
import org.languagetool.language.English;
import org.languagetool.rules.Rule;
import org.languagetool.rules.en.MorfologikAmericanSpellerRule;
import org.languagetool.tokenizers.Tokenizer;
import software.amazon.smithy.utils.SmithyInternalApi;

/**
 * Default Implementation of English language for models.
 * <p>
 * Note: Based on implementation in <a href="https://github.com/aha-oretama/TypoFixer">TypoFixer</a>.
 */
@SuppressWarnings("deprecation")
@SmithyInternalApi
public final class CodingEnglish extends English {
    private static final List<String> EXTRA_TERMS = List.of(
            "docstring",
            "doc",
            "api",
            "sdk");

    @Override
    public Tokenizer createDefaultWordTokenizer() {
        return new CodeTokenizer();
    }

    @Override
    public List<Rule> getRelevantRules(
            ResourceBundle messages,
            UserConfig userConfig,
            Language motherTongue,
            List<Language> altLanguages
    ) throws IOException {
        List<Rule> rules = new ArrayList<>(super.getRelevantRules(messages, userConfig, motherTongue, altLanguages));
        var spellchecker = new MorfologikAmericanSpellerRule(messages, this);
        spellchecker.addIgnoreTokens(EXTRA_TERMS);
        rules.add(spellchecker);
        return rules;
    }

    /**
     * Service provider to instantiate a language checker.
     */
    public static final class Provider extends LanguageService.Provider {
        public Provider() {
            super("en", CodingEnglish::new);
        }
    }
}
