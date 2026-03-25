package com.github.pramodkuth.openpolicy.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.pramodkuth.openpolicy.advisor.DefaultOpenPolicyAdvisorBuilder;
import com.github.pramodkuth.openpolicy.advisor.OpenPolicyAdvisor;
import com.github.pramodkuth.openpolicy.client.OpaClient;
import com.github.pramodkuth.openpolicy.client.OpaClientFallback;
import com.github.pramodkuth.openpolicy.client.OpaFallbackClientHttpResponse;
import com.github.pramodkuth.openpolicy.model.DefaultPromptEvaluationRequest;
import com.github.pramodkuth.openpolicy.model.DefaultToolEvaluationRequest;
import com.github.pramodkuth.openpolicy.model.resolver.DefaultEvaluationResponseResolver;
import com.github.pramodkuth.openpolicy.model.resolver.DefaultPromptEvaluationRequestResolver;
import com.github.pramodkuth.openpolicy.model.resolver.DefaultToolsEvaluationRequestResolver;
import com.github.pramodkuth.openpolicy.model.resolver.EvaluationResponseResolver;
import com.github.pramodkuth.openpolicy.model.resolver.PromptEvaluationRequestResolver;
import com.github.pramodkuth.openpolicy.model.resolver.ToolsEvaluationRequestResolver;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import io.github.resilience4j.core.functions.CheckedSupplier;
import io.github.resilience4j.retry.RetryRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.web.client.RestClientSsl;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestClient;

import java.io.IOException;
import java.util.Objects;
import java.util.Optional;

/**
 * Autoconfiguration for Open policy AI advisor.
 *
 * @author pramodkuth
 */
@Configuration
@EnableConfigurationProperties(OpaAdvisorsProperties.class)
public class OpenPolicyAiAdvisorAutoConfiguration {
    private static final Logger logger = LoggerFactory.getLogger(OpenPolicyAiAdvisorAutoConfiguration.class);
    ObjectMapper mapper = new ObjectMapper();

    /**
     * Creates a {@link RestClient} bean with the specified properties and resilience settings.
     *
     * @param builder               builder for creating the rest client
     * @param advisorsProperties    properties for configuring the OPA advisor
     * @param restClientSsl         ssl configuration for the rest client
     * @param cbRegistryProvider    circuit breaker registry provider
     * @param retryRegistryProvider retry registry provider
     * @param fallbackProvider      fallback provider
     * @return configured RestClient bean
     */
    @Bean("opaRestClient")
    public RestClient opaRestClient(RestClient.Builder builder,
                                    OpaAdvisorsProperties advisorsProperties,
                                    RestClientSsl restClientSsl,
                                    ObjectProvider<CircuitBreakerRegistry> cbRegistryProvider,
                                    ObjectProvider<RetryRegistry> retryRegistryProvider,
                                    ObjectProvider<OpaClientFallback> fallbackProvider) {
        OpaAdvisorsProperties.OpaAgentProperties props = advisorsProperties.getAgent();

        builder.baseUrl(props.getHost());

        if (StringUtils.hasText(props.getSslBundle())) {
            builder.apply(restClientSsl.fromBundle(props.getSslBundle()));
        }

        var circuitBreakerRegistry = cbRegistryProvider.getIfAvailable();
        var retryRegistry = retryRegistryProvider.getIfAvailable();
        var resiliencyInstance = props.getResilience4jInstance();

        if (StringUtils.hasText(resiliencyInstance)) {
            var cbOpt = Optional.ofNullable(circuitBreakerRegistry).flatMap(cbr -> cbr.find(resiliencyInstance));
            var retryOpt = Optional.ofNullable(retryRegistry).flatMap(rtr -> rtr.find(resiliencyInstance));
            builder.requestInterceptor((request, body, execution) ->
                    {
                        try {
                            CheckedSupplier<ClientHttpResponse> supplier = () -> execution.execute(request, body);
                            if (cbOpt.isPresent()) {
                                supplier = cbOpt.get().decorateCheckedSupplier(supplier);
                            }
                            if (retryOpt.isPresent()) {
                                supplier = retryOpt.get().decorateCheckedSupplier(supplier);
                            }

                            return supplier.get();
                        } catch (Throwable e) {
                            var fallback = fallbackProvider.getIfAvailable();
                            if (Objects.nonNull(fallback)) {
                                var fallbackValue = fallback.fallback(e);

                                byte[] content = mapper.writeValueAsBytes(fallbackValue);
                                return new OpaFallbackClientHttpResponse(content, HttpStatus.OK);
                            } else {
                                logger.error("Request to OPA failed:{}", e.getMessage(), e);
                                throw new IOException("Request to OPA failed", e);
                            }
                        }
                    }
            );
        }
        logger.debug("Rest client initialized with host: {}", props.getHost());

        return builder.build();
    }

    /**
     * Creates an {@link OpaClient} bean using the provided rest client, request and response resolvers.
     *
     * @param restClient            configured RestClient bean
     * @param promptRequestResolver resolver for creating prompt evaluation requests
     * @param toolRequestResolver   resolver for creating tool evaluation requests
     * @param responseResolver      resolver for handling evaluation responses
     * @return configured OpaClient bean
     */
    @Bean
    public OpaClient opaClient(@Qualifier("opaRestClient") RestClient restClient, PromptEvaluationRequestResolver<DefaultPromptEvaluationRequest> promptRequestResolver, ToolsEvaluationRequestResolver<DefaultToolEvaluationRequest> toolRequestResolver, EvaluationResponseResolver responseResolver) {
        return new OpaClient(restClient, promptRequestResolver, toolRequestResolver, responseResolver);
    }

    /**
     * Creates a {@link OpenPolicyAdvisor.Builder} bean using the provided properties and client.
     *
     * @param properties properties for configuring the OPA advisor
     * @param client     OPA client instance
     * @return configured OpenPolicyAdvisor.Builder bean
     */
    @Bean
    public OpenPolicyAdvisor.Builder builder(OpaAdvisorsProperties properties, OpaClient client) {
        return new DefaultOpenPolicyAdvisorBuilder(properties, client);
    }

    /**
     * Creates a default {@link PromptEvaluationRequestResolver} bean if one is not already provided.
     *
     * @return default PromptEvaluationRequestResolver bean
     */
    @ConditionalOnMissingBean(PromptEvaluationRequestResolver.class)
    @Bean
    public PromptEvaluationRequestResolver<DefaultPromptEvaluationRequest> defaultEvaluationPromptResolver() {
        return new DefaultPromptEvaluationRequestResolver();
    }

    /**
     * Creates a default {@link ToolsEvaluationRequestResolver} bean if one is not already provided.
     *
     * @return default ToolsEvaluationRequestResolver bean
     */
    @ConditionalOnMissingBean(ToolsEvaluationRequestResolver.class)
    @Bean
    public ToolsEvaluationRequestResolver<DefaultToolEvaluationRequest> defaultEvaluationToolsResolver() {
        return new DefaultToolsEvaluationRequestResolver();
    }

    /**
     * Creates a default {@link EvaluationResponseResolver} bean if one is not already provided.
     *
     * @return default EvaluationResponseResolver bean
     */
    @ConditionalOnMissingBean(EvaluationResponseResolver.class)
    @Bean
    public EvaluationResponseResolver defaultEvaluationResponseResolver() {
        return new DefaultEvaluationResponseResolver();
    }

}