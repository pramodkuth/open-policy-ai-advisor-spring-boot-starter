# Open Policy AI Advisor Spring Boot Starter

Open Policy AI Advisor is a Spring Boot starter that integrates Open Policy Agent (OPA) for policy evaluation and enforcement in your applications. This starter simplifies the process of adding open policy-based advisors capabilities to your AI chat client.

## Table of Contents

- [Setup](#setup)
- [Bean configuration](#bean-configuration)
  - [Example 1: Advisor and Chat client config](#example-1advisor-and-chat-client-configuration) 
- [Usage](#usage)
  - [Example 1: Basic Policy Evaluation](#example-1-basic-policy-evaluation)
- [Contributing](#contributing)

## Setup

To use this starter in your Spring Boot project, follow these steps:

1. Add the dependency to your `pom.xml` file:
    ```xml
    <dependency>
        <groupId>com.github.pramodkuth</groupId>
        <artifactId>open-policy-ai-advisor-spring-boot-starter</artifactId>
        <version>1.0.0</version>
    </dependency>
    ```

2. Configure the OPA advisors in your `application.yml` or `application.properties` file:
    ```yaml
    open-policy:
      agent:
         host: http://localhost:8181
         resilience4j-instance: opa
         advisors:
             my-prompt-safety-advisor:
                 policies:
                     - name: gaurd delete tools
                       path: v1/data/ai/allow
                       guard-tools: true
   resilience4j:
     circuitbreaker:
       instances:
         opa:
          sliding-window-size: 10
          failure-rate-threshold: 50
     retry:
       instances:
         opa:
          max-attempts: 3
          wait-duration: 500ms
       ```
## Bean configuration
### Example 1:Advisor and chat client configuration
```java
import com.github.pramodkuth.openpolicy.advisor.OpenPolicyAdvisor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.api.CallAdvisor;
import org.springframework.ai.ollama.OllamaChatModel;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ChatConfig {
    @Bean
    public ChatClient chatClient(OllamaChatModel model, ToolCallbackProvider toolCallbackProvider, CallAdvisor advisor) {
        return ChatClient.builder(model).defaultAdvisors(advisor).defaultToolCallbacks(toolCallbackProvider).build();
    }

    @Bean
    public CallAdvisor advisor(OpenPolicyAdvisor.Builder builder) {
        return builder.advisor("my-prompt-safety-advisor").build();
    }
}

```
## Usage

### Example 1: Basic Policy Evaluation

Here is an example of how to use the starter for basic policy evaluation:

```java
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class PolicyController {

    @Autowired
    private ChatClient chatClient;

    @PostMapping("/chat")
    public String evaluatePolicy(@RequestBody PromptRequest promptRequest) {
        String res = chatClient.prompt(promptRequest.prompt).call().content();
        return ResponseEntity.ok(res);
    }
    
    public static class PromptRequest{
        private String prompt;
    }
}
```

## Contributing

Contributions are welcome! Please read the [CONTRIBUTING](CONTRIBUTING.md) file for guidelines on how to contribute to this project.