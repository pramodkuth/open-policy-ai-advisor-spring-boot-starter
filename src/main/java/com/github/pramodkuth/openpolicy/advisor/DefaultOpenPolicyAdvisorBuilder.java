package com.github.pramodkuth.openpolicy.advisor;

import com.github.pramodkuth.openpolicy.client.OpaClient;
import com.github.pramodkuth.openpolicy.config.OpaAdvisorsProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.advisor.api.Advisor;
import org.springframework.util.Assert;

import java.util.NoSuchElementException;
import java.util.Optional;

/**
 * Builder class to construct instances of {@link OpenPolicyAdvisor}.
 * @author pramodkuth
 */
public class DefaultOpenPolicyAdvisorBuilder implements OpenPolicyAdvisor.Builder {

    private static final Logger logger = LoggerFactory.getLogger(DefaultOpenPolicyAdvisorBuilder.class);

    /**
     * The properties for the OPA advisors.
     */
    private final OpaAdvisorsProperties properties;

    /**
     * The OPA client to use for making policy evaluations.
     */
    private final OpaClient opaClient;

    /**
     * The name of this advisor.
     */
    private String name;

    /**
     * The order in which this advisor should be executed.
     * Default {@code Advisor.HIGHEST_PRECEDENCE}
     */
    private Integer order = Advisor.HIGHEST_PRECEDENCE;

    /**
     * The specific advisor instance to use.
     */
    private String advisor;

    /**
     * Creates a new instance of {@link DefaultOpenPolicyAdvisorBuilder} with the given properties and OPA client.
     *
     * @param properties the properties for the OPA advisors
     * @param opaClient  the OPA client to use for making policy evaluations
     */
    public DefaultOpenPolicyAdvisorBuilder(OpaAdvisorsProperties properties, OpaClient opaClient) {
        this.properties = properties;
        this.opaClient = opaClient;
    }

    /**
     * Sets the name of this advisor.
     *
     * @param name the name to set
     * @return a reference to this builder instance for method chaining
     */
    @Override
    public OpenPolicyAdvisor.Builder name(String name) {
        this.name = name;
        return this;
    }

    /**
     * Sets the order in which this advisor should be executed.
     *
     * @param order the order to set
     * @return a reference to this builder instance for method chaining
     */
    @Override
    public OpenPolicyAdvisor.Builder order(Integer order) {
        this.order = order;
        return this;
    }

    /**
     * Sets the specific advisor instance to use.
     *
     * @param advisor the advisor instance to set
     * @return a reference to this builder instance for method chaining
     */
    @Override
    public OpenPolicyAdvisor.Builder advisor(String advisor) {
        this.advisor = advisor;
        return this;
    }

    /**
     * Builds and returns an instance of {@link OpenPolicyAdvisor} using the current configuration.
     *
     * @return a new instance of {@link OpenPolicyAdvisor}
     */
    @Override
    public OpenPolicyAdvisor build() {
        logger.debug("Building OpenPolicyAdvisor with properties: {}, opaClient: {}, name: {}, order: {}, advisor: {}", properties, opaClient, name, order, advisor);

        Assert.notNull(advisor, "advisor cannot be null or empty");
        var opaProperties = Optional.of(properties).map(OpaAdvisorsProperties::getAdvisors)
                .map(advisorMap -> advisorMap.get(advisor))
                .orElseThrow(() -> new NoSuchElementException("Could not find the given advisor instance: " + advisor));
        return new DefaultOpenPolicySafetyAdvisor(opaClient, opaProperties, name, order);
    }
}
