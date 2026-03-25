package com.github.pramodkuth.openpolicy.client;

import com.fasterxml.jackson.databind.JsonNode;

/**
 * Functional interface for providing a fallback implementation in case the main OPA client call fails.
 * @author pramodkuth
 */
@FunctionalInterface
public interface OpaClientFallback {
    /**
     * Handles the fallback logic when the main OPA client call throws an exception.
     *
     * @param t the Throwable that occurred during the main OPA client call
     * @return a JsonNode representing the fallback response
     */
    JsonNode fallback(Throwable t);
}
