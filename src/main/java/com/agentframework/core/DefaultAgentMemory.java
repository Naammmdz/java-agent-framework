package com.agentframework.core;

import com.agentframework.communication.Message;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Default in-memory implementation of AgentMemory.
 * This implementation stores everything in memory and is suitable for development and testing.
 */
public class DefaultAgentMemory implements AgentMemory {
    
    private final List<Message> messages = new CopyOnWriteArrayList<>();
    private final List<String> goals = new CopyOnWriteArrayList<>();
    private final Map<String, Object> keyValueStore = new ConcurrentHashMap<>();
    private final int maxMessages;
    
    public DefaultAgentMemory() {
        this(1000); // Default to storing last 1000 messages
    }
    
    public DefaultAgentMemory(int maxMessages) {
        this.maxMessages = maxMessages;
    }
    
    @Override
    public void storeMessage(@NotNull Message message) {
        messages.add(message);
        
        // Remove old messages if we exceed the limit
        while (messages.size() > maxMessages) {
            messages.remove(0);
        }
    }
    
    @NotNull
    @Override
    public List<Message> getRecentMessages(int limit) {
        int size = messages.size();
        if (size == 0) {
            return List.of();
        }
        
        int startIndex = Math.max(0, size - limit);
        return List.copyOf(messages.subList(startIndex, size));
    }
    
    @Override
    public void storeGoal(@NotNull String goal) {
        goals.add(goal);
    }
    
    @NotNull
    @Override
    public List<String> getGoals() {
        return List.copyOf(goals);
    }
    
    @Override
    public void store(@NotNull String key, @NotNull Object value) {
        keyValueStore.put(key, value);
    }
    
    @Nullable
    @Override
    public Object retrieve(@NotNull String key) {
        return keyValueStore.get(key);
    }
    
    @Override
    public boolean remove(@NotNull String key) {
        return keyValueStore.remove(key) != null;
    }
    
    @Override
    public void clear() {
        messages.clear();
        goals.clear();
        keyValueStore.clear();
    }
    
    @NotNull
    @Override
    public MemoryStats getStats() {
        // Rough estimation of memory usage
        long totalSize = messages.size() * 200L + // Estimate 200 bytes per message
                        goals.size() * 50L +      // Estimate 50 bytes per goal
                        keyValueStore.size() * 100L; // Estimate 100 bytes per key-value pair
        
        return new MemoryStats(
            messages.size(),
            goals.size(),
            keyValueStore.size(),
            totalSize
        );
    }
}
