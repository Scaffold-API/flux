package com.scaffold.api.plugins.spellcheck;

import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.languagetool.markup.AnnotatedText;
import org.languagetool.markup.AnnotatedTextBuilder;

final class AnnotationUtils {
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
            "UL"
    );
    private AnnotationUtils() {}

    public static AnnotatedText annotateText(String text) {
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
}
