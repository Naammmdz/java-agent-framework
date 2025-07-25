package com.agentframework.ai;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * Interface for AI model integration.
 * Supports various AI providers like OpenAI, Anthropic, local models, etc.
 */
public interface AIModel {
    
    /**
     * Generates a response to the given prompt
     * @param prompt the input prompt
     * @return CompletableFuture containing the AI response
     */
    @NotNull
    CompletableFuture<AIResponse> generateResponse(@NotNull String prompt);
    
    /**
     * Generates a response with additional parameters
     * @param prompt the input prompt
     * @param parameters additional parameters (temperature, max_tokens, etc.)
     * @return CompletableFuture containing the AI response
     */
    @NotNull
    CompletableFuture<AIResponse> generateResponse(@NotNull String prompt, @NotNull AIParameters parameters);
    
    /**
     * Continues a conversation with message history
     * @param messages conversation history
     * @return CompletableFuture containing the AI response
     */
    @NotNull
    CompletableFuture<AIResponse> continueConversation(@NotNull List<ChatMessage> messages);
    
    /**
     * Continues a conversation with parameters
     * @param messages conversation history
     * @param parameters additional parameters
     * @return CompletableFuture containing the AI response
     */
    @NotNull
    CompletableFuture<AIResponse> continueConversation(@NotNull List<ChatMessage> messages, @NotNull AIParameters parameters);
    
    /**
     * Checks if the model supports function calling
     * @return true if function calling is supported
     */
    boolean supportsFunctionCalling();
    
    /**
     * Generates a response with function calling capability
     * @param messages conversation history
     * @param functions available functions
     * @param parameters additional parameters
     * @return CompletableFuture containing the AI response
     */
    @NotNull
    CompletableFuture<AIResponse> generateWithFunctions(@NotNull List<ChatMessage> messages, 
                                                       @NotNull List<AIFunction> functions, 
                                                       @NotNull AIParameters parameters);
    
    /**
     * Gets information about this AI model
     * @return model information
     */
    @NotNull
    ModelInfo getModelInfo();
    
    /**
     * Checks if the model is available and healthy
     * @return true if the model is available
     */
    boolean isAvailable();
    
    /**
     * Represents a chat message in a conversation
     */
    class ChatMessage {
        private final String role; // "system", "user", "assistant", "function"
        private final String content;
        private final String name; // Optional: for function messages
        private final String functionCall; // Optional: for function calling
        
        public ChatMessage(@NotNull String role, @NotNull String content) {
            this(role, content, null, null);
        }
        
        public ChatMessage(@NotNull String role, @NotNull String content, @Nullable String name, @Nullable String functionCall) {
            this.role = role;
            this.content = content;
            this.name = name;
            this.functionCall = functionCall;
        }
        
        @NotNull
        public String getRole() {
            return role;
        }
        
        @NotNull
        public String getContent() {
            return content;
        }
        
        @Nullable
        public String getName() {
            return name;
        }
        
        @Nullable
        public String getFunctionCall() {
            return functionCall;
        }
        
        public static ChatMessage system(@NotNull String content) {
            return new ChatMessage("system", content);
        }
        
        public static ChatMessage user(@NotNull String content) {
            return new ChatMessage("user", content);
        }
        
        public static ChatMessage assistant(@NotNull String content) {
            return new ChatMessage("assistant", content);
        }
        
        public static ChatMessage function(@NotNull String name, @NotNull String content) {
            return new ChatMessage("function", content, name, null);
        }
    }
    
    /**
     * Represents an AI function that can be called
     */
    class AIFunction {
        private final String name;
        private final String description;
        private final Map<String, Object> parameters;
        
        public AIFunction(@NotNull String name, @NotNull String description, @NotNull Map<String, Object> parameters) {
            this.name = name;
            this.description = description;
            this.parameters = Map.copyOf(parameters);
        }
        
        @NotNull
        public String getName() {
            return name;
        }
        
        @NotNull
        public String getDescription() {
            return description;
        }
        
        @NotNull
        public Map<String, Object> getParameters() {
            return parameters;
        }
    }
    
    /**
     * Parameters for AI model requests
     */
    class AIParameters {
        private final double temperature;
        private final int maxTokens;
        private final double topP;
        private final double frequencyPenalty;
        private final double presencePenalty;
        private final List<String> stop;
        private final Map<String, Object> additionalParams;
        
        private AIParameters(Builder builder) {
            this.temperature = builder.temperature;
            this.maxTokens = builder.maxTokens;
            this.topP = builder.topP;
            this.frequencyPenalty = builder.frequencyPenalty;
            this.presencePenalty = builder.presencePenalty;
            this.stop = builder.stop != null ? List.copyOf(builder.stop) : List.of();
            this.additionalParams = Map.copyOf(builder.additionalParams);
        }
        
        public static Builder builder() {
            return new Builder();
        }
        
        public static AIParameters defaultParams() {
            return new Builder().build();
        }
        
        public double getTemperature() {
            return temperature;
        }
        
        public int getMaxTokens() {
            return maxTokens;
        }
        
        public double getTopP() {
            return topP;
        }
        
        public double getFrequencyPenalty() {
            return frequencyPenalty;
        }
        
        public double getPresencePenalty() {
            return presencePenalty;
        }
        
        @NotNull
        public List<String> getStop() {
            return stop;
        }
        
        @NotNull
        public Map<String, Object> getAdditionalParams() {
            return additionalParams;
        }
        
        public static class Builder {
            private double temperature = 0.7;
            private int maxTokens = 1000;
            private double topP = 1.0;
            private double frequencyPenalty = 0.0;
            private double presencePenalty = 0.0;
            private List<String> stop = List.of();
            private Map<String, Object> additionalParams = Map.of();
            
            public Builder temperature(double temperature) {
                this.temperature = temperature;
                return this;
            }
            
            public Builder maxTokens(int maxTokens) {
                this.maxTokens = maxTokens;
                return this;
            }
            
            public Builder topP(double topP) {
                this.topP = topP;
                return this;
            }
            
            public Builder frequencyPenalty(double frequencyPenalty) {
                this.frequencyPenalty = frequencyPenalty;
                return this;
            }
            
            public Builder presencePenalty(double presencePenalty) {
                this.presencePenalty = presencePenalty;
                return this;
            }
            
            public Builder stop(@NotNull List<String> stop) {
                this.stop = stop;
                return this;
            }
            
            public Builder additionalParam(@NotNull String key, @NotNull Object value) {
                this.additionalParams = Map.of(key, value);
                return this;
            }
            
            public AIParameters build() {
                return new AIParameters(this);
            }
        }
    }
    
    /**
     * Response from an AI model
     */
    class AIResponse {
        private final String content;
        private final String finishReason;
        private final int totalTokens;
        private final int promptTokens;
        private final int completionTokens;
        private final String functionCall;
        private final Map<String, Object> metadata;
        
        public AIResponse(@NotNull String content, @Nullable String finishReason, 
                         int totalTokens, int promptTokens, int completionTokens,
                         @Nullable String functionCall, @Nullable Map<String, Object> metadata) {
            this.content = content;
            this.finishReason = finishReason;
            this.totalTokens = totalTokens;
            this.promptTokens = promptTokens;
            this.completionTokens = completionTokens;
            this.functionCall = functionCall;
            this.metadata = metadata != null ? Map.copyOf(metadata) : Map.of();
        }
        
        @NotNull
        public String getContent() {
            return content;
        }
        
        @Nullable
        public String getFinishReason() {
            return finishReason;
        }
        
        public int getTotalTokens() {
            return totalTokens;
        }
        
        public int getPromptTokens() {
            return promptTokens;
        }
        
        public int getCompletionTokens() {
            return completionTokens;
        }
        
        @Nullable
        public String getFunctionCall() {
            return functionCall;
        }
        
        @NotNull
        public Map<String, Object> getMetadata() {
            return metadata;
        }
        
        public boolean isFunctionCall() {
            return functionCall != null && !functionCall.isEmpty();
        }
    }
    
    /**
     * Information about the AI model
     */
    class ModelInfo {
        private final String name;
        private final String provider;
        private final String version;
        private final int maxContextLength;
        private final boolean supportsChat;
        private final boolean supportsFunctions;
        private final Map<String, Object> capabilities;
        
        public ModelInfo(@NotNull String name, @NotNull String provider, @NotNull String version,
                        int maxContextLength, boolean supportsChat, boolean supportsFunctions,
                        @Nullable Map<String, Object> capabilities) {
            this.name = name;
            this.provider = provider;
            this.version = version;
            this.maxContextLength = maxContextLength;
            this.supportsChat = supportsChat;
            this.supportsFunctions = supportsFunctions;
            this.capabilities = capabilities != null ? Map.copyOf(capabilities) : Map.of();
        }
        
        @NotNull
        public String getName() {
            return name;
        }
        
        @NotNull
        public String getProvider() {
            return provider;
        }
        
        @NotNull
        public String getVersion() {
            return version;
        }
        
        public int getMaxContextLength() {
            return maxContextLength;
        }
        
        public boolean supportsChat() {
            return supportsChat;
        }
        
        public boolean supportsFunctions() {
            return supportsFunctions;
        }
        
        @NotNull
        public Map<String, Object> getCapabilities() {
            return capabilities;
        }
    }
}
