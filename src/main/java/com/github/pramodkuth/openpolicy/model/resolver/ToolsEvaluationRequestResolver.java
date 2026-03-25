package com.github.pramodkuth.openpolicy.model.resolver;

import org.springframework.ai.chat.messages.AssistantMessage;

/**
 * Functional interface for resolving tools evaluation requests based on an {@link  AssistantMessage.ToolCall}.
 * @author pramodkkuth
 */
@FunctionalInterface
public interface ToolsEvaluationRequestResolver<R> {
    /**
     * Resolves the tools evaluation request from the given {@link  AssistantMessage.ToolCall} and produces a result of type R.
     *
     * @param input the {@link  AssistantMessage.ToolCall} containing the tool call information
     * @return the evaluation request created by resolving the {@link  AssistantMessage.ToolCall}.
     */
    R resolve(AssistantMessage.ToolCall input);
}
