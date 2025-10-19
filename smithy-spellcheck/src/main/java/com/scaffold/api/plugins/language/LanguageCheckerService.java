/*
 * Copyright Scaffold Software LLC. All Rights Reserved.
 * SPDX-License-Identifier: Apache-2.0
 */
package com.scaffold.api.plugins.language;

import java.util.Map;
import java.util.Optional;
import java.util.ServiceLoader;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Represents a language checker used to evaluate the spelling and grammar of models.
 */
public interface LanguageCheckerService {
    Map<String, LanguageCheckerService> PROVIDERS = ServiceLoader.load(
            LanguageCheckerService.class,
            LanguageCheckerService.class.getClassLoader())
            .stream()
            .map(ServiceLoader.Provider::get)
            .collect(Collectors.toMap(LanguageCheckerService::getLanguageCode, Function.identity()));

    /**
     * Get a language checker instance for the given language.
     *
     * @param code language code to get instance for.
     * @return instantiated {@link LanguageChecker}.
     */
    static LanguageChecker expect(String code) {
        return Optional.ofNullable(PROVIDERS.get(code))
                .map(LanguageCheckerService::create)
                .orElseThrow(() -> new RuntimeException("Expected language provider for code: " + code));
    }

    /**
     * @return Unique code used to identify the language.
     */
    String getLanguageCode();

    /**
     * Instantiate a new language checker.
     *
     * @return Instantiated language checker.
     */
    LanguageChecker create();
}
