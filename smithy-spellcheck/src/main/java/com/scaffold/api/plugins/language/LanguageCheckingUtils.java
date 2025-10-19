/*
 * Copyright Scaffold Software LLC. All Rights Reserved.
 * SPDX-License-Identifier: Apache-2.0
 */
package com.scaffold.api.plugins.language;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.languagetool.JLanguageTool;
import org.languagetool.markup.AnnotatedText;
import org.languagetool.markup.AnnotatedTextBuilder;
import org.languagetool.rules.RuleMatch;
import org.languagetool.rules.spelling.SpellingCheckRule;

/**
 * Utilities for checking text in Models.
 */
final class LanguageCheckingUtils {
    // Used to detect HTML tags in the docs
    private static final Pattern TAG_PATTERN = Pattern.compile("<([a-z]+)*>.*?</\\1>", Pattern.DOTALL);
    // Tags whose contents are still checked by linters
    private static final Set<String> CHECKED_TAGS = Set.of(
            "BLOCKQUOTE",
            "BR",
            "CAPTION",
            "CENTER",
            "DD",
            "FIGCAPTION",
            "H1",
            "H2",
            "H3",
            "H4",
            "H5",
            "H6",
            "HTML",
            "I",
            "LI",
            "META",
            "P",
            "SECTION",
            "SMALL",
            "SPAN",
            "STRIKE",
            "STRONG",
            "TITLE",
            "TT",
            "U",
            "UL");
    private LanguageCheckingUtils() { /* Utility Class */ }

    /**
     * Get all potential problems matched by a rule
     *
     * @param tool LanguageTool instance
     * @param text Text to check
     * @return list of all matched rules
     */
    static List<RuleMatch> getMatches(JLanguageTool tool, String text) {
        try {
            return tool.check(annotateText(text));
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private static AnnotatedText annotateText(String text) {
        var builder = new AnnotatedTextBuilder();
        annotateText(text, builder);
        return builder.build();
    }

    // TODO: also check {```} markdown blocks
    private static void annotateText(String text, AnnotatedTextBuilder builder) {
        // Split out any HTML-tag wrapped sections
        Matcher matcher = TAG_PATTERN.matcher(text);
        int lastMatchPos = 0;
        System.out.println();
        while (matcher.find()) {
            // add all plain text contents up to the match
            builder.addText(text.substring(lastMatchPos, matcher.start()));

            // Check inner text if the tag is supported for checking. Otherwise,
            // treat full tag as markdown.
            var htmlTag = matcher.group(1);
            if (CHECKED_TAGS.contains(htmlTag.toUpperCase())) {
                builder.addMarkup("<" + htmlTag + ">");
                var offsetForTagStart = 2 + htmlTag.length();
                var tagContents = text.substring(matcher.start() + offsetForTagStart, matcher.end());
                // Check nested text for additional tags.
                annotateText(tagContents, builder);
            } else {
                builder.addMarkup(text.substring(matcher.start(), matcher.end()));
            }
            lastMatchPos = matcher.end();
        }
        // Write out all remaining
        builder.addText(text.substring(lastMatchPos));
    }

    /**
     * Disables all non-spellcheck rules and sets any ignored tokens.
     *
     * @param ignored - Tokens to ignore when spell checking
     */
    static void configureSpellcheck(JLanguageTool tool, List<String> ignored) {
        for (var rule : tool.getAllActiveRules()) {
            if (rule instanceof SpellingCheckRule scr) {
                scr.addIgnoreTokens(ignored);
            } else {
                // Deactivate any non-spellcheck rules
                tool.disableRule(rule.getId());
            }
        }
    }

}
