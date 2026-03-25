package com.github.pramodkuth.openpolicy.model.resolver;

import com.github.pramodkuth.openpolicy.model.DefaultToolEvaluationRequest;
import org.springframework.ai.chat.messages.AssistantMessage;

/**
 * Default implementation of {@link ToolsEvaluationRequestResolver} that resolves the default tools evaluation request accepted by the OPA.
 * @author pramodkuth
 */
public class DefaultToolsEvaluationRequestResolver implements ToolsEvaluationRequestResolver<DefaultToolEvaluationRequest> {

    /**
     * Resolves the tools evaluation request from the given {@code AssistantMessage.ToolCall}.
     *
     * @param toolCall the {@code AssistantMessage.ToolCall} to resolve
     * @return a new instance of DefaultToolEvaluationRequest containing the resolved tool call input
     */
    @Override
    public DefaultToolEvaluationRequest resolve(AssistantMessage.ToolCall toolCall) {
        var input = new DefaultToolEvaluationRequest.DefaultEvaluationInput(toolCall.name(), toolCall.arguments());
        return new DefaultToolEvaluationRequest(input);
    }

}
