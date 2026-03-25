package com.github.pramodkuth.openpolicy.model;

import java.util.Map;

/**
 * Represents the default prompt evaluation request accepted by the OPA.
 * @author pramodkuth
 */
public record DefaultPromptEvaluationRequest(DefaultEvaluationInput input) {

    /**
     * Represents default prompt evaluation request for evaluations.
     */
    public record DefaultEvaluationInput(
            String userPrompt,
            String systemPrompt,
            Map<String, Object> metadata) {
    }
}
