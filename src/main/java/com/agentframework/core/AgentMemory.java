package com.agentframework.core;

import com.agentframework.communication.Message;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * Interface for agent memory/knowledge storage and retrieval.
 */
public interface AgentMemory {
    
    /**
     * Stores a message in memory
     * @param message the message to store
     */
    void storeMessage(@NotNull Message message);
    
    /**
     * Retrieves recent messages
     * @param limit maximum number of messages to retrieve
     * @return list of recent messages
     */
    @NotNull
    List<Message> getRecentMessages(int limit);
    
    /**
     * Stores a goal in memory
     * @param goal the goal to store
     */
    void storeGoal(@NotNull String goal);
    
    /**
     * Gets all stored goals
     * @return list of goals
     */
    @NotNull
    List<String> getGoals();
    
    /**
     * Stores a key-value pair in memory
     * @param key the key
     * @param value the value
     */
    void store(@NotNull String key, @NotNull Object value);
    
    /**
     * Retrieves a value by key
     * @param key the key to look up
     * @return the stored value, or null if not found
     */
    @Nullable
    Object retrieve(@NotNull String key);
    
    /**
     * Removes a key-value pair from memory
     * @param key the key to remove
     * @return true if the key was removed, false if it didn't exist
     */
    boolean remove(@NotNull String key);
    
    /**
     * Clears all memory
     */
    void clear();
    
    /**
     * Gets the current memory usage statistics
     * @return memory statistics
     */
    @NotNull
    MemoryStats getStats();
    
    /**
     * Memory usage statistics
     */
    class MemoryStats {
        private final int messageCount;
        private final int goalCount;
        private final int keyValueCount;
        private final long totalSize;
        
        public MemoryStats(int messageCount, int goalCount, int keyValueCount, long totalSize) {
            this.messageCount = messageCount;
            this.goalCount = goalCount;
            this.keyValueCount = keyValueCount;
            this.totalSize = totalSize;
        }
        
        public int getMessageCount() {
            return messageCount;
        }
        
        public int getGoalCount() {
            return goalCount;
        }
        
        public int getKeyValueCount() {
            return keyValueCount;
        }
        
        public long getTotalSize() {
            return totalSize;
        }
        
        @Override
        public String toString() {
            return String.format("MemoryStats{messages=%d, goals=%d, keyValues=%d, totalSize=%d}",
                               messageCount, goalCount, keyValueCount, totalSize);
        }
    }
}
