# AI Provider Integration Examples

This document provides examples of how to integrate various AI providers with the Java Agent Framework.

## Table of Contents
- [OpenAI Integration](#openai-integration)
- [Anthropic Integration](#anthropic-integration)
- [Azure OpenAI Integration](#azure-openai-integration)
- [Local Model Integration](#local-model-integration)
- [Custom REST API Integration](#custom-rest-api-integration)

## OpenAI Integration

### Dependencies
Add to your `pom.xml`:
```xml
<dependency>
    <groupId>com.theokanning.openai-gpt3-java</groupId>
    <artifactId>service</artifactId>
    <version>0.18.2</version>
</dependency>
```

### Implementation
```java
package com.example.providers;

import com.agentframework.ai.AIModel;
import com.theokanning.openai.completion.chat.*;
import com.theokanning.openai.service.OpenAiService;
import org.jetbrains.annotations.NotNull;

import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

public class OpenAIProvider implements AIModel {
    private final OpenAiService service;
    private final String modelName;
    private final ModelInfo modelInfo;

    public OpenAIProvider(String apiKey, String modelName) {
        this.service = new OpenAiService(apiKey, Duration.ofSeconds(30));
        this.modelName = modelName;
        this.modelInfo = createModelInfo(modelName);
    }

    @Override
    public CompletableFuture<AIResponse> generateResponse(String prompt) {
        return generateResponse(prompt, AIParameters.defaultParams());
    }

    @Override
    public CompletableFuture<AIResponse> generateResponse(String prompt, AIParameters parameters) {
        List<ChatMessage> messages = List.of(ChatMessage.user(prompt));
        return continueConversation(messages, parameters);
    }

    @Override
    public CompletableFuture<AIResponse> continueConversation(List<ChatMessage> messages) {
        return continueConversation(messages, AIParameters.defaultParams());
    }

    @Override
    public CompletableFuture<AIResponse> continueConversation(List<ChatMessage> messages, AIParameters parameters) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                List<com.theokanning.openai.completion.chat.ChatMessage> openAiMessages = 
                    messages.stream()
                        .map(this::convertToOpenAiMessage)
                        .collect(Collectors.toList());

                ChatCompletionRequest request = ChatCompletionRequest.builder()
                    .model(modelName)
                    .messages(openAiMessages)
                    .temperature(parameters.getTemperature())
                    .maxTokens(parameters.getMaxTokens())
                    .build();

                ChatCompletionResult result = service.createChatCompletion(request);
                return convertToAIResponse(result);
            } catch (Exception e) {
                throw new RuntimeException("Failed to generate response from OpenAI", e);
            }
        });
    }

    @Override
    public boolean supportsFunctionCalling() {
        return modelName.startsWith("gpt-3.5-turbo") || modelName.startsWith("gpt-4");
    }

    @Override
    public CompletableFuture<AIResponse> generateWithFunctions(List<ChatMessage> messages, 
                                                              List<AIFunction> functions, 
                                                              AIParameters parameters) {
        // Implementation for function calling
        throw new UnsupportedOperationException("Function calling implementation needed");
    }

    @Override
    public ModelInfo getModelInfo() {
        return modelInfo;
    }

    @Override
    public boolean isAvailable() {
        try {
            service.listModels();
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    private com.theokanning.openai.completion.chat.ChatMessage convertToOpenAiMessage(ChatMessage message) {
        String role = message.getRole().toLowerCase();
        return new com.theokanning.openai.completion.chat.ChatMessage(role, message.getContent());
    }

    private AIResponse convertToAIResponse(ChatCompletionResult result) {
        var choice = result.getChoices().get(0);
        var message = choice.getMessage();
        
        String content = message.getContent() != null ? message.getContent() : "";
        int totalTokens = result.getUsage() != null ? (int) result.getUsage().getTotalTokens() : 0;
        int promptTokens = result.getUsage() != null ? (int) result.getUsage().getPromptTokens() : 0;
        int completionTokens = result.getUsage() != null ? (int) result.getUsage().getCompletionTokens() : 0;
        
        Map<String, Object> metadata = Map.of(
            "model", result.getModel(),
            "id", result.getId()
        );
        
        return new AIResponse(content, choice.getFinishReason(), totalTokens, 
                             promptTokens, completionTokens, null, metadata);
    }

    private ModelInfo createModelInfo(String modelName) {
        int maxContextLength = switch (modelName) {
            case "gpt-3.5-turbo" -> 4096;
            case "gpt-4" -> 8192;
            case "gpt-4-turbo-preview" -> 128000;
            default -> 4096;
        };
        
        return new ModelInfo(modelName, "OpenAI", "1.0", maxContextLength, 
                           true, supportsFunctionCalling(), Map.of());
    }
}
```

### Usage
```java
AIModel openAI = new OpenAIProvider("your-api-key", "gpt-3.5-turbo");
Agent agent = new AgentBuilder()
    .withName("OpenAIAgent")
    .withAIModel(openAI)
    .build();
```

## Anthropic Integration

### Dependencies
```xml
<dependency>
    <groupId>your.preferred</groupId>
    <artifactId>anthropic-client</artifactId>
    <version>x.x.x</version>
</dependency>
```

### Implementation
```java
package com.example.providers;

import com.agentframework.ai.AIModel;
// Import your Anthropic client library

public class AnthropicProvider implements AIModel {
    private final AnthropicClient client;
    private final String modelName;

    public AnthropicProvider(String apiKey, String modelName) {
        this.client = new AnthropicClient(apiKey);
        this.modelName = modelName;
    }

    @Override
    public CompletableFuture<AIResponse> generateResponse(String prompt) {
        return CompletableFuture.supplyAsync(() -> {
            // Use your Anthropic client to make the request
            AnthropicResponse response = client.complete(prompt, modelName);
            
            return new AIResponse(
                response.getContent(),
                "stop",
                response.getTokenUsage().getTotal(),
                response.getTokenUsage().getPrompt(),
                response.getTokenUsage().getCompletion(),
                null,
                Map.of("model", modelName)
            );
        });
    }

    // Implement other required methods...

    @Override
    public boolean supportsFunctionCalling() {
        return modelName.contains("claude-3"); // Adjust based on model capabilities
    }

    @Override
    public ModelInfo getModelInfo() {
        return new ModelInfo(modelName, "Anthropic", "1.0", 100000, true, false, Map.of());
    }

    @Override
    public boolean isAvailable() {
        try {
            // Perform health check
            return client.isHealthy();
        } catch (Exception e) {
            return false;
        }
    }
}
```

## Azure OpenAI Integration

### Dependencies
```xml
<dependency>
    <groupId>com.azure</groupId>
    <artifactId>azure-ai-openai</artifactId>
    <version>1.0.0-beta.6</version>
</dependency>
```

### Implementation
```java
package com.example.providers;

import com.agentframework.ai.AIModel;
import com.azure.ai.openai.OpenAIClient;
import com.azure.ai.openai.OpenAIClientBuilder;
import com.azure.ai.openai.models.*;
import com.azure.core.credential.AzureKeyCredential;

public class AzureOpenAIProvider implements AIModel {
    private final OpenAIClient client;
    private final String deploymentName;

    public AzureOpenAIProvider(String endpoint, String apiKey, String deploymentName) {
        this.client = new OpenAIClientBuilder()
            .endpoint(endpoint)
            .credential(new AzureKeyCredential(apiKey))
            .buildClient();
        this.deploymentName = deploymentName;
    }

    @Override
    public CompletableFuture<AIResponse> generateResponse(String prompt) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                ChatCompletionsOptions options = new ChatCompletionsOptions(
                    List.of(new ChatRequestUserMessage(prompt))
                );

                ChatCompletions response = client.getChatCompletions(deploymentName, options);
                ChatChoice choice = response.getChoices().get(0);
                
                return new AIResponse(
                    choice.getMessage().getContent(),
                    choice.getFinishReason().toString(),
                    response.getUsage().getTotalTokens(),
                    response.getUsage().getPromptTokens(),
                    response.getUsage().getCompletionTokens(),
                    null,
                    Map.of("model", deploymentName)
                );
            } catch (Exception e) {
                throw new RuntimeException("Failed to generate response from Azure OpenAI", e);
            }
        });
    }

    // Implement other required methods...
}
```

## Local Model Integration

### Example with Ollama
```java
package com.example.providers;

import com.agentframework.ai.AIModel;
import okhttp3.*;
import com.fasterxml.jackson.databind.ObjectMapper;

public class OllamaProvider implements AIModel {
    private final OkHttpClient httpClient;
    private final String baseUrl;
    private final String modelName;
    private final ObjectMapper objectMapper;

    public OllamaProvider(String baseUrl, String modelName) {
        this.httpClient = new OkHttpClient();
        this.baseUrl = baseUrl;
        this.modelName = modelName;
        this.objectMapper = new ObjectMapper();
    }

    @Override
    public CompletableFuture<AIResponse> generateResponse(String prompt) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                Map<String, Object> requestBody = Map.of(
                    "model", modelName,
                    "prompt", prompt,
                    "stream", false
                );

                RequestBody body = RequestBody.create(
                    objectMapper.writeValueAsString(requestBody),
                    MediaType.get("application/json")
                );

                Request request = new Request.Builder()
                    .url(baseUrl + "/api/generate")
                    .post(body)
                    .build();

                try (Response response = httpClient.newCall(request).execute()) {
                    if (!response.isSuccessful()) {
                        throw new RuntimeException("Request failed: " + response.code());
                    }

                    Map<String, Object> responseData = objectMapper.readValue(
                        response.body().string(), Map.class
                    );

                    return new AIResponse(
                        (String) responseData.get("response"),
                        "stop",
                        0, // Ollama doesn't provide token counts
                        0,
                        0,
                        null,
                        Map.of("model", modelName)
                    );
                }
            } catch (Exception e) {
                throw new RuntimeException("Failed to generate response from Ollama", e);
            }
        });
    }

    // Implement other required methods...

    @Override
    public boolean supportsFunctionCalling() {
        return false; // Most local models don't support function calling
    }

    @Override
    public ModelInfo getModelInfo() {
        return new ModelInfo(modelName, "Ollama", "1.0", 4096, true, false, Map.of());
    }

    @Override
    public boolean isAvailable() {
        try {
            Request request = new Request.Builder()
                .url(baseUrl + "/api/tags")
                .get()
                .build();

            try (Response response = httpClient.newCall(request).execute()) {
                return response.isSuccessful();
            }
        } catch (Exception e) {
            return false;
        }
    }
}
```

## Custom REST API Integration

### Generic REST API Provider
```java
package com.example.providers;

import com.agentframework.ai.AIModel;
import okhttp3.*;
import com.fasterxml.jackson.databind.ObjectMapper;

public class CustomAPIProvider implements AIModel {
    private final OkHttpClient httpClient;
    private final String apiUrl;
    private final String apiKey;
    private final String modelName;
    private final ObjectMapper objectMapper;

    public CustomAPIProvider(String apiUrl, String apiKey, String modelName) {
        this.httpClient = new OkHttpClient();
        this.apiUrl = apiUrl;
        this.apiKey = apiKey;
        this.modelName = modelName;
        this.objectMapper = new ObjectMapper();
    }

    @Override
    public CompletableFuture<AIResponse> generateResponse(String prompt) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                // Customize this based on your API's expected format
                Map<String, Object> requestBody = Map.of(
                    "model", modelName,
                    "messages", List.of(Map.of("role", "user", "content", prompt)),
                    "max_tokens", 1000,
                    "temperature", 0.7
                );

                RequestBody body = RequestBody.create(
                    objectMapper.writeValueAsString(requestBody),
                    MediaType.get("application/json")
                );

                Request request = new Request.Builder()
                    .url(apiUrl)
                    .header("Authorization", "Bearer " + apiKey)
                    .header("Content-Type", "application/json")
                    .post(body)
                    .build();

                try (Response response = httpClient.newCall(request).execute()) {
                    if (!response.isSuccessful()) {
                        throw new RuntimeException("API request failed: " + response.code());
                    }

                    // Parse response based on your API's format
                    Map<String, Object> responseData = objectMapper.readValue(
                        response.body().string(), Map.class
                    );

                    // Extract the generated text - adjust based on your API's response format
                    List<Map<String, Object>> choices = (List<Map<String, Object>>) responseData.get("choices");
                    Map<String, Object> message = (Map<String, Object>) choices.get(0).get("message");
                    String content = (String) message.get("content");

                    // Extract usage if available
                    Map<String, Object> usage = (Map<String, Object>) responseData.get("usage");
                    int totalTokens = usage != null ? (Integer) usage.getOrDefault("total_tokens", 0) : 0;
                    int promptTokens = usage != null ? (Integer) usage.getOrDefault("prompt_tokens", 0) : 0;
                    int completionTokens = usage != null ? (Integer) usage.getOrDefault("completion_tokens", 0) : 0;

                    return new AIResponse(
                        content,
                        "stop",
                        totalTokens,
                        promptTokens,
                        completionTokens,
                        null,
                        Map.of("model", modelName)
                    );
                }
            } catch (Exception e) {
                throw new RuntimeException("Failed to generate response from custom API", e);
            }
        });
    }

    // Implement other required methods...

    @Override
    public boolean supportsFunctionCalling() {
        // Return true if your API supports function calling
        return false;
    }

    @Override
    public ModelInfo getModelInfo() {
        return new ModelInfo(modelName, "CustomAPI", "1.0", 4096, true, false, Map.of());
    }

    @Override
    public boolean isAvailable() {
        try {
            // Implement a health check for your API
            Request request = new Request.Builder()
                .url(apiUrl.replace("/chat/completions", "/models")) // Adjust endpoint
                .header("Authorization", "Bearer " + apiKey)
                .get()
                .build();

            try (Response response = httpClient.newCall(request).execute()) {
                return response.isSuccessful();
            }
        } catch (Exception e) {
            return false;
        }
    }
}
```

## Using Your Custom Provider

Once you've implemented your AI provider, you can use it with the framework:

```java
public class Example {
    public static void main(String[] args) {
        // Choose your provider
        AIModel aiModel = new OpenAIProvider("your-api-key", "gpt-3.5-turbo");
        // or
        // AIModel aiModel = new AnthropicProvider("your-api-key", "claude-3-sonnet");
        // or
        // AIModel aiModel = new OllamaProvider("http://localhost:11434", "llama2");

        // Create agent with your provider
        Agent agent = new AgentBuilder()
            .withName("MyAgent")
            .withDescription("A helpful assistant")
            .withAIModel(aiModel)
            .withSystemPrompt("You are a helpful assistant...")
            .build();

        // Use the agent
        agent.start().get();
        
        Message message = Message.builder()
            .from(UUID.randomUUID())
            .to(agent.getId())
            .type("request")
            .content("Hello, how can you help me?")
            .build();

        Message response = agent.processMessage(message).get();
        System.out.println("Agent: " + response.getContent());

        agent.stop().get();
    }
}
```

## Best Practices

1. **Error Handling**: Always wrap API calls in try-catch blocks and provide meaningful error messages.

2. **Timeouts**: Set appropriate timeouts for HTTP requests to prevent hanging.

3. **Rate Limiting**: Implement rate limiting if your AI provider has usage limits.

4. **Caching**: Consider caching responses for identical prompts to reduce API costs.

5. **Configuration**: Make parameters like API endpoints, timeouts, and model names configurable.

6. **Health Checks**: Implement proper health checks in the `isAvailable()` method.

7. **Token Management**: Handle token usage and limits appropriately.

8. **Async Processing**: Use CompletableFuture properly to avoid blocking operations.

This framework is designed to be provider-agnostic, so you can easily switch between different AI services or use multiple providers simultaneously in your application.
