package com.github.pramodkuth.openpolicy.model;

/**
 * Represents the default tool call evaluation request accepted by the OPA.
 */
public record DefaultToolEvaluationRequest(DefaultEvaluationInput input) {

    /**
     * Represents default input for evaluations.
     */
    public record DefaultEvaluationInput(
            String tool,
            String args) {
    }
}
