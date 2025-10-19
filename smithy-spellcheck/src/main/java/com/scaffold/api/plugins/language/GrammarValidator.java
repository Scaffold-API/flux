/*
 * Copyright Scaffold Software LLC. All Rights Reserved.
 * SPDX-License-Identifier: Apache-2.0
 */
package com.scaffold.api.plugins.language;

import java.util.List;
import software.amazon.smithy.model.Model;
import software.amazon.smithy.model.validation.AbstractValidator;
import software.amazon.smithy.model.validation.ValidationEvent;
import software.amazon.smithy.utils.SmithyInternalApi;

@SmithyInternalApi
public final class GrammarValidator extends AbstractValidator {

    @Override
    public List<ValidationEvent> validate(Model model) {
        return List.of();
    }

}
