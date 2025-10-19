/*
 * Copyright Scaffold Software LLC. All Rights Reserved.
 * SPDX-License-Identifier: Apache-2.0
 */
package com.scaffold.api.plugins.language;

import java.util.ServiceLoader;
import java.util.function.Supplier;
import org.languagetool.JLanguageTool;
import org.languagetool.Language;

/**
 * Represents a language checker used to evaluate the spelling and grammar of models.
 */
public interface LanguageService {
    /**
     * @return Unique code used to identify the language.
     */
    String getLanguageCode();

    /**
     * Instantiate a new language.
     *
     * @return Instantiated language.
     */
    Language create();

    /**
     * Get a language tool instance for the given language.
     *
     * @param code language code to get instance for.
     * @return instantiated {@link JLanguageTool}.
     */
    static Language load(String code, ClassLoader loader) {
        return ServiceLoader.load(LanguageService.class, loader)
                .stream()
                .map(ServiceLoader.Provider::get)
                .filter(p -> p.getLanguageCode().equals(code))
                .reduce((a, b) -> {
                    throw new RuntimeException("Expected only one implementation for Lanuage " + code
                            + " but found multiple");
                })
                .map(LanguageService::create)
                .orElseThrow(() -> new RuntimeException("Expected language provider for code: " + code));
    }

    /**
     * Provides a simple abstraction for creating validator service subclasses
     */
    abstract class Provider implements LanguageService {
        private final String languageCode;
        private final Supplier<? extends Language> supplier;

        public Provider(String languageCode, Supplier<? extends Language> supplier) {
            this.languageCode = languageCode;
            this.supplier = supplier;
        }

        @Override
        public final String getLanguageCode() {
            return this.languageCode;
        }

        @Override
        public final Language create() {
            return this.supplier.get();
        }
    }
}
