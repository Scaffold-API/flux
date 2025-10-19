/*
 * Copyright Scaffold Software LLC. All Rights Reserved.
 * SPDX-License-Identifier: Apache-2.0
 */
package com.scaffold.api.plugins.language;

import java.util.Map;
import java.util.Optional;
import java.util.ServiceLoader;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import org.languagetool.JLanguageTool;
import org.languagetool.Language;

/**
 * Represents a language checker used to evaluate the spelling and grammar of models.
 */
public interface LanguageToolService {
    Map<String, LanguageToolService> PROVIDERS = ServiceLoader.load(
            LanguageToolService.class,
            LanguageToolService.class.getClassLoader())
            .stream()
            .map(ServiceLoader.Provider::get)
            .collect(Collectors.toMap(LanguageToolService::getLanguageCode, Function.identity()));

    /**
     * Get a language tool instance for the given language.
     *
     * @param code language code to get instance for.
     * @return instantiated {@link JLanguageTool}.
     */
    static JLanguageTool expect(String code) {
        return Optional.ofNullable(PROVIDERS.get(code))
                .map(LanguageToolService::create)
                .orElseThrow(() -> new RuntimeException("Expected language provider for code: " + code));
    }

    /**
     * @return Unique code used to identify the language.
     */
    String getLanguageCode();

    /**
     * Instantiate a new language tool.
     *
     * @return Instantiated language tool.
     */
    JLanguageTool create();

    /**
     * Provides a simple abstraction for creating validator service subclasses
     */
    abstract class Provider implements LanguageToolService {
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

        @Override
        public JLanguageTool create() {
            return new JLanguageTool(this.supplier.get());
        }
    }
}
