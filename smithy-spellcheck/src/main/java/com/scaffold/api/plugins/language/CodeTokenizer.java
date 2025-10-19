/*
 * Copyright Scaffold Software LLC. All Rights Reserved.
 * SPDX-License-Identifier: Apache-2.0
 */
package com.scaffold.api.plugins.language;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.StringTokenizer;
import org.languagetool.tokenizers.WordTokenizer;

/**
 * Tokenizer for code.
 * <p>
 * Splits camel, snake, and kebab-cased variables into words.
 */
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
