package com.github.pramodkuth.openpolicy.model.resolver;


import com.fasterxml.jackson.databind.JsonNode;
/**
 * Functional interface for resolving evaluation responses based on a JSON node response.
 */
@FunctionalInterface
public interface EvaluationResponseResolver {
    /**
     * Check for the OPA response and determines whether the policy evaluation result is allowed or not.
     *
     * @param root the JSON node representing the evaluation response
     * @return true if the evaluation response result is true, false otherwise
     */
    boolean resolve(JsonNode root);
}
