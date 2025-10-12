package com.scaffold.api.plugins.spellcheck;

import com.google.rpc.Code;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import org.languagetool.Language;
import org.languagetool.UserConfig;
import org.languagetool.language.AmericanEnglish;
import org.languagetool.language.English;
import org.languagetool.rules.Rule;
import org.languagetool.rules.en.MorfologikAmericanSpellerRule;
import org.languagetool.tokenizers.Tokenizer;
import org.languagetool.tokenizers.WordTokenizer;

// Create a provider for languages??
public class CodingEnglish extends English {
    // TODO: Add a custom smithy tokenizer?
    private static final WordTokenizer TOKENIZER = new CodeTokenizer();

    @Override
    public Tokenizer createDefaultWordTokenizer() {
        return TOKENIZER;
    }

    @Override
    public List<Rule> getRelevantRules(ResourceBundle messages, UserConfig userConfig, Language motherTongue, List<Language> altLanguages) throws IOException {
        List<Rule> rules = new ArrayList<>(super.getRelevantRules(messages, userConfig, motherTongue, altLanguages));
        rules.add(new MorfologikAmericanSpellerRule(messages, this));
        return rules;
    }
}
