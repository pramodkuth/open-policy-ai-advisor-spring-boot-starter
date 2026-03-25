package com.github.pramodkuth.openpolicy.model.resolver;

import com.github.pramodkuth.openpolicy.model.DefaultPromptEvaluationRequest;
import org.springframework.ai.chat.prompt.Prompt;

import java.util.Map;

/**
 * Default implementation of {@link PromptEvaluationRequestResolver} that resolves prompt evaluation requests accepted by the OPA.
 */
public class DefaultPromptEvaluationRequestResolver implements PromptEvaluationRequestResolver<DefaultPromptEvaluationRequest> {

    /**
     * Resolves the prompt evaluation request from the given Prompt and produces a new instance of {@link DefaultPromptEvaluationRequest}.
     *
     * @param prompt the Prompt containing the prompt information
     * @return a new instance of DefaultPromptEvaluationRequest with the resolved prompt input
     */
    @Override
    public DefaultPromptEvaluationRequest resolve(Prompt prompt) {
        String userMsg = prompt.getUserMessage().getText();
        String sysMsg = prompt.getSystemMessage().getText();
        var input = new DefaultPromptEvaluationRequest.DefaultEvaluationInput(userMsg, sysMsg, Map.of());
        return new DefaultPromptEvaluationRequest(input);
    }

}
