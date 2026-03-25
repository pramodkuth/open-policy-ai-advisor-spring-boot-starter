package com.github.pramodkuth.openpolicy.advisor;

import org.springframework.ai.chat.client.advisor.api.CallAdvisor;

/**
 * Interface for an Open Policy Advisor that extends {@link CallAdvisor}.
 * @author pramodkuth
 */
public interface OpenPolicyAdvisor extends CallAdvisor {

    /**
     * Builder interface to construct instances of {@link OpenPolicyAdvisor}.
     */
    interface Builder {
        /**
         * Sets the name of this advisor.
         *
         * @param name the name to set
         * @return a reference to this builder instance for method chaining
         */
        Builder name(String name);

        /**
         * Sets the order in which this advisor should be executed.
         *
         * @param order the order to set
         * @return a reference to this builder instance for method chaining
         */
        Builder order(Integer order);

        /**
         * Sets the specific advisor instance to use.
         *
         * @param advisor the advisor instance to set
         * @return a reference to this builder instance for method chaining
         */
        Builder advisor(String advisor);

        /**
         * Builds and returns an instance of {@link OpenPolicyAdvisor} using the current configuration.
         *
         * @return a new instance of {@link OpenPolicyAdvisor}
         */
        OpenPolicyAdvisor build();
    }
}
