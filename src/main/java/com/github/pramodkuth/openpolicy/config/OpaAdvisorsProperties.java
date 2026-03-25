package com.github.pramodkuth.openpolicy.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Configuration properties for the OPA Advisor(Open Policy Agent Advisor).
 */
@ConfigurationProperties(prefix = "open-policy")
@Data
public class OpaAdvisorsProperties {
    private OpaAgentProperties agent;
    private Map<String, OpaProperties> advisors;

    /**
     * Configuration properties for the OPA server(Open Policy Agent Server) .
     */
    @Data
    public static class OpaAgentProperties {
        /**
         * The host of the OPA server.
         */
        private String host;
        /**
         * SSL bundle
         */
        private String sslBundle;

        /**
         * The reference to the circuit breaker instance(resilience4j).
         */
        public String resilience4jInstance;
    }
    /**
     * Configuration properties for the policies.
     */
    @Data
    public static class OpaProperties {
        /**
         * Policy settings for the OPA client.
         */
        private List<OpaAdvisorsProperties.OpaProperties.Policy> policies = new ArrayList<>();
        /**
         * Policy settings for the OPA client.
         */
        @Data
        public static class Policy {

            /**
             * The name of the policy config.
             */
            private String name;

            /**
             * The path to the policy config.
             */
            private String path;

            /**
             * Whether to guard prompts with OPA.
             */
            private boolean guardPrompt;

            /**
             * Whether to guard tools with OPA.
             */
            private boolean guardTools;
        }
    }
}
