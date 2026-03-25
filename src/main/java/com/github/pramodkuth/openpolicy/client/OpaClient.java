package com.github.pramodkuth.openpolicy.client;

import com.fasterxml.jackson.databind.JsonNode;
import com.github.pramodkuth.openpolicy.model.resolver.EvaluationResponseResolver;
import com.github.pramodkuth.openpolicy.model.resolver.PromptEvaluationRequestResolver;
import com.github.pramodkuth.openpolicy.model.resolver.ToolsEvaluationRequestResolver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.http.MediaType;
import org.springframework.web.client.RestClient;

/**
 * Client to interact with the Open Policy Agent (OPA) for evaluating prompts and tool calls.
 * @author pramodkuth
 */
public class OpaClient {

private static final Logger logger = LoggerFactory.getLogger(OpaClient.class);

    /**
     * The RestClient used to make HTTP requests to the OPA server.
     */
    private final RestClient restClient;

    /**
     * Resolver for creating prompt evaluation requests.
     */
    private final PromptEvaluationRequestResolver<?> promptRequestResolver;

    /**
     * Resolver for creating tools evaluation requests.
     */
    private final ToolsEvaluationRequestResolver<?> toolRequestResolver;

    /**
     * Resolver for interpreting the response from OPA.
     */
    private final EvaluationResponseResolver responseResolver;

    /**
     * Constructs an instance of OpaClient with the given properties and resolvers.
     *
     * @param restClient RestClient for the OPA client.
     * @param requestResolver Resolver for creating prompt evaluation requests.
     * @param toolRequestResolver Resolver for creating tools evaluation requests.
     * @param responseResolver Resolver for interpreting the response from OPA.
     */
    public OpaClient(RestClient restClient, PromptEvaluationRequestResolver<?> requestResolver, ToolsEvaluationRequestResolver<?> toolRequestResolver, EvaluationResponseResolver responseResolver) {
        this.restClient = restClient;
        this.promptRequestResolver = requestResolver;
        this.toolRequestResolver = toolRequestResolver;
        this.responseResolver = responseResolver;
    }

    /**
     * Checks if the given prompt is allowed based on OPA policies.
     *
     * @param prompt The prompt to evaluate.
     * @return {@code true} if the prompt is allowed, {@code false} otherwise.
     */
    public boolean isAllowed(Prompt prompt, String policyPath) {
        logger.debug("Checking if prompt is allowed");
        Object request = promptRequestResolver.resolve(prompt);
        JsonNode root = this.restClient.post().uri(policyPath)
                .contentType(MediaType.APPLICATION_JSON)
                .body(request).retrieve()
                .body(JsonNode.class);
        return responseResolver.resolve(root);
    }

    /**
     * Checks if the given tool call is allowed based on OPA policies.
     *
     * @param toolCall The tool call to evaluate.
     * @return {@code true} if the tool call is allowed, {@code false} otherwise.
     */
    public boolean isAllowed(AssistantMessage.ToolCall toolCall, String policyPath) {
        logger.debug("Checking if tool call is allowed");
        Object request = toolRequestResolver.resolve(toolCall);
        JsonNode root = this.restClient.post().uri(policyPath)
                .contentType(MediaType.APPLICATION_JSON)
                .body(request).retrieve()
                .body(JsonNode.class);
        return responseResolver.resolve(root);
    }

}
