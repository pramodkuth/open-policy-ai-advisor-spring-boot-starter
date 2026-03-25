package com.github.pramodkuth.openpolicy.model.resolver;

import org.springframework.ai.chat.prompt.Prompt;

/**
 * Functional interface for resolving prompt evaluation requests based on a {@link Prompt}.
 * @author pramodkuth
 */
@FunctionalInterface
public interface PromptEvaluationRequestResolver<R> {
    /**
     * Resolves the prompt evaluation request from the given Prompt and produces a result of type R.
     *
     * @param input the {@link Prompt} containing the prompt information
     * @return the policy evaluation request created by resolving the prompt.
     */
    R resolve(Prompt input);
}
