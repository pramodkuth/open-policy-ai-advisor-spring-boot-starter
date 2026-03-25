package com.github.pramodkuth.openpolicy.model.resolver;

import com.fasterxml.jackson.databind.JsonNode;

/**
 * Default implementation of {@link EvaluationResponseResolver} that resolves the policy evaluation response from OPA.
 * @author pramodkuth
 */
public class DefaultEvaluationResponseResolver implements EvaluationResponseResolver {

    /**
     * Check for the OPA response and determines whether the policy evaluation result is allowed or not.
     *
     * @param root the JSON node representing the evaluation response
     * @return true if the evaluation response result is true, false otherwise
     */
    @Override
    public boolean resolve(JsonNode root) {
        return root.path("result").asBoolean();
    }
}
