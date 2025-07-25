package com.agentframework.tools;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * Interface for tools that agents can use to interact with their environment.
 * Tools provide specific capabilities like file operations, API calls, calculations, etc.
 */
public interface Tool {
    
    /**
     * Gets the unique name of this tool
     * @return tool name
     */
    @NotNull
    String getName();
    
    /**
     * Gets a human-readable description of what this tool does
     * @return tool description
     */
    @NotNull
    String getDescription();
    
    /**
     * Gets the parameter schema for this tool
     * @return JSON schema describing the expected parameters
     */
    @NotNull
    String getParameterSchema();
    
    /**
     * Executes this tool with the given parameters
     * @param parameters input parameters for the tool
     * @return CompletableFuture containing the execution result
     */
    @NotNull
    CompletableFuture<ToolResult> execute(@Nullable Map<String, Object> parameters);
    
    /**
     * Validates whether the given parameters are valid for this tool
     * @param parameters parameters to validate
     * @return true if parameters are valid, false otherwise
     */
    boolean validateParameters(@Nullable Map<String, Object> parameters);
    
    /**
     * Checks if this tool is currently available for use
     * @return true if available, false otherwise
     */
    boolean isAvailable();
    
    /**
     * Gets metadata about this tool
     * @return tool metadata
     */
    @NotNull
    ToolMetadata getMetadata();
    
    /**
     * Result of tool execution
     */
    class ToolResult {
        private final boolean success;
        private final Object data;
        private final String error;
        private final Map<String, Object> metadata;
        
        public ToolResult(boolean success, @Nullable Object data, @Nullable String error, 
                         @Nullable Map<String, Object> metadata) {
            this.success = success;
            this.data = data;
            this.error = error;
            this.metadata = metadata != null ? Map.copyOf(metadata) : Map.of();
        }
        
        public static ToolResult success(@NotNull Object data) {
            return new ToolResult(true, data, null, null);
        }
        
        public static ToolResult success(@NotNull Object data, @NotNull Map<String, Object> metadata) {
            return new ToolResult(true, data, null, metadata);
        }
        
        public static ToolResult error(@NotNull String error) {
            return new ToolResult(false, null, error, null);
        }
        
        public boolean isSuccess() {
            return success;
        }
        
        @Nullable
        public Object getData() {
            return data;
        }
        
        @Nullable
        public String getError() {
            return error;
        }
        
        @NotNull
        public Map<String, Object> getMetadata() {
            return metadata;
        }
        
        @Override
        public String toString() {
            return String.format("ToolResult{success=%s, data=%s, error='%s'}", 
                               success, data, error);
        }
    }
    
    /**
     * Metadata about a tool
     */
    class ToolMetadata {
        private final String category;
        private final String version;
        private final boolean requiresAuth;
        private final Map<String, Object> properties;
        
        public ToolMetadata(@NotNull String category, @NotNull String version, 
                           boolean requiresAuth, @Nullable Map<String, Object> properties) {
            this.category = category;
            this.version = version;
            this.requiresAuth = requiresAuth;
            this.properties = properties != null ? Map.copyOf(properties) : Map.of();
        }
        
        @NotNull
        public String getCategory() {
            return category;
        }
        
        @NotNull
        public String getVersion() {
            return version;
        }
        
        public boolean requiresAuth() {
            return requiresAuth;
        }
        
        @NotNull
        public Map<String, Object> getProperties() {
            return properties;
        }
    }
}
