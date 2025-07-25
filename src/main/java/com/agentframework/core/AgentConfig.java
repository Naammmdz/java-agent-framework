package com.agentframework.core;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;

/**
 * Configuration class for agents containing runtime parameters and settings.
 */
public class AgentConfig {
    
    private final long executionInterval;
    private final int maxConcurrentTasks;
    private final boolean enableLogging;
    private final Map<String, Object> properties;
    
    private AgentConfig(Builder builder) {
        this.executionInterval = builder.executionInterval;
        this.maxConcurrentTasks = builder.maxConcurrentTasks;
        this.enableLogging = builder.enableLogging;
        this.properties = Map.copyOf(builder.properties);
    }
    
    public static Builder builder() {
        return new Builder();
    }
    
    public static AgentConfig defaultConfig() {
        return new Builder().build();
    }
    
    public long getExecutionInterval() {
        return executionInterval;
    }
    
    public int getMaxConcurrentTasks() {
        return maxConcurrentTasks;
    }
    
    public boolean isLoggingEnabled() {
        return enableLogging;
    }
    
    @NotNull
    public Map<String, Object> getProperties() {
        return properties;
    }
    
    @Nullable
    public Object getProperty(@NotNull String key) {
        return properties.get(key);
    }
    
    @NotNull
    public <T> T getProperty(@NotNull String key, @NotNull T defaultValue) {
        @SuppressWarnings("unchecked")
        T value = (T) properties.get(key);
        return value != null ? value : defaultValue;
    }
    
    public static class Builder {
        private long executionInterval = 1000; // 1 second
        private int maxConcurrentTasks = 10;
        private boolean enableLogging = true;
        private Map<String, Object> properties = new HashMap<>();
        
        public Builder executionInterval(long intervalMs) {
            this.executionInterval = intervalMs;
            return this;
        }
        
        public Builder maxConcurrentTasks(int maxTasks) {
            this.maxConcurrentTasks = maxTasks;
            return this;
        }
        
        public Builder enableLogging(boolean enable) {
            this.enableLogging = enable;
            return this;
        }
        
        public Builder property(@NotNull String key, @NotNull Object value) {
            this.properties.put(key, value);
            return this;
        }
        
        public Builder properties(@NotNull Map<String, Object> properties) {
            this.properties.putAll(properties);
            return this;
        }
        
        public AgentConfig build() {
            return new AgentConfig(this);
        }
    }
    
    @Override
    public String toString() {
        return String.format("AgentConfig{executionInterval=%d, maxConcurrentTasks=%d, enableLogging=%s}",
                           executionInterval, maxConcurrentTasks, enableLogging);
    }
}
