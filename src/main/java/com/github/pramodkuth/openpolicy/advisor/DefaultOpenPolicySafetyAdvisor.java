package com.github.pramodkuth.openpolicy.advisor;

import com.github.pramodkuth.openpolicy.client.OpaClient;
import com.github.pramodkuth.openpolicy.config.OpaAdvisorsProperties;
import jakarta.annotation.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClientRequest;
import org.springframework.ai.chat.client.ChatClientResponse;
import org.springframework.ai.chat.client.advisor.api.CallAdvisor;
import org.springframework.ai.chat.client.advisor.api.CallAdvisorChain;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.model.Generation;
import org.springframework.util.Assert;

import java.util.Collection;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Predicate;

/**
 * Advisor implementation that uses Open Policy Agent (OPA) to evaluate prompts and tool calls.
 *
 * @author pramodkuth
 */
public class DefaultOpenPolicySafetyAdvisor implements OpenPolicyAdvisor {
    private static final Logger logger = LoggerFactory.getLogger(DefaultOpenPolicySafetyAdvisor.class);

    /**
     * The OPA client used for making policy evaluations.
     */
    private final OpaClient opaClient;

    /**
     * The properties for the open policies.
     */
    private final OpaAdvisorsProperties.OpaProperties opaProps;

    /**
     * The name of this advisor.
     */
    private final String name;

    /**
     * The order in which this advisor should be executed.
     */
    private final Integer order;

    /**
     * Creates a new instance of {@link DefaultOpenPolicySafetyAdvisor}.
     *
     * @param opaClient the OPA client used for making policy evaluations
     * @param opaProps  the properties for the open policies
     * @param name      the name of this advisor
     * @param order     the order in which this advisor should be executed
     */
    public DefaultOpenPolicySafetyAdvisor(OpaClient opaClient, OpaAdvisorsProperties.OpaProperties opaProps, @Nullable String name, @Nullable Integer order) {
        this.opaClient = opaClient;
        this.opaProps = opaProps;
        this.name = name;
        this.order = order;
    }

    /**
     * Advises the execution of a chat client request by evaluating prompts and tool calls using OPA.
     *
     * @param chatClientRequest the chat client request to advise
     * @param callAdvisorChain  the chain of call advisors to use for further processing
     * @return the advised chat client response
     */
    @Override
    public ChatClientResponse adviseCall(ChatClientRequest chatClientRequest, CallAdvisorChain callAdvisorChain) {
        Assert.notNull(chatClientRequest, "chatClientRequest cannot be null");
        Assert.notNull(callAdvisorChain, "callAdvisorChain cannot be null");
        logger.debug("Advisor called for prompt: {}", chatClientRequest.prompt().getUserMessage().getText());

        var policies = opaProps.getPolicies();

        policies.stream()
                .filter(OpaAdvisorsProperties.OpaProperties.Policy::isGuardPrompt)
                .filter(policy -> !opaClient.isAllowed(chatClientRequest.prompt(), policy.getPath())).findFirst()
                .ifPresent(promptRequest -> {
                    logger.error("Unauthorized prompt: {}", chatClientRequest.prompt().getUserMessage().getText());
                    throw new SecurityException("Unauthorized prompt: " + chatClientRequest.prompt().getUserMessage().getText());
                });
        logger.debug("Prompt authorized, checking for tools");

        var modelResponse = callAdvisorChain.nextCall(chatClientRequest);
        var chatResponse = modelResponse.chatResponse();
        for (var policy : policies) {
            if (policy.isGuardTools() && Objects.nonNull(chatResponse) && chatResponse.hasToolCalls()) {
                chatResponse.getResults().stream()
                        .map(Generation::getOutput)
                        .map(AssistantMessage::getToolCalls)
                        .flatMap(Collection::stream).filter(toolCall -> !opaClient.isAllowed(toolCall, policy.getPath()))
                        .findFirst().ifPresent(toolCall -> {
                            logger.error("Unauthorized tool execution: {}", toolCall.name());
                            throw new SecurityException("OPA: Unauthorized tool execution: " + toolCall.name());
                        });
            }
        }
        return modelResponse;
    }

    /**
     * Returns the name of this advisor.
     *
     * @return the name of this advisor. Default class name.
     */
    @Override
    public String getName() {
        return Optional.ofNullable(name).filter(Predicate.not(String::isBlank)).orElseGet(() -> this.getClass().getName());
    }

    /**
     * Returns the order in which this advisor should be executed.
     *
     * @return the order in which this advisor should be executed. Default {@code CallAdvisor.HIGHEST_PRECEDENCE}
     */
    @Override
    public int getOrder() {
        return Optional.ofNullable(order).orElse(CallAdvisor.HIGHEST_PRECEDENCE);
    }
}
