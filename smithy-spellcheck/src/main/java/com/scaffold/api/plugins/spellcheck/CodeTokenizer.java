package com.scaffold.api.plugins.spellcheck;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.StringTokenizer;
import org.languagetool.tokenizers.WordTokenizer;

// TODO: Add credit
public final class CodeTokenizer extends WordTokenizer {
    @Override
    public String getTokenizingCharacters() {
        return super.getTokenizingCharacters() + "–_@";
    }

    @Override
    public List<String> tokenize(String text) {
        List<String> list = new ArrayList<>();
        var tokenizer = new StringTokenizer(text, this.getTokenizingCharacters(), true);
        while (tokenizer.hasMoreElements()) {
            Collections.addAll(list, tokenizer.nextToken().split("(?<!(^|[A-Z]))(?=[A-Z])|(?<!^)(?=[A-Z][a-z])"));
        }
        return this.joinEMailsAndUrls(list);
    }
}
