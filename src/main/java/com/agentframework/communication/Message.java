package com.agentframework.communication;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Represents a message that can be sent between agents or from the environment.
 * Messages are immutable and thread-safe.
 */
public final class Message {
    
    private final UUID id;
    private final UUID senderId;
    private final UUID receiverId;
    private final String type;
    private final Object content;
    private final Map<String, Object> metadata;
    private final Instant timestamp;
    private final Priority priority;
    
    @JsonCreator
    public Message(
            @JsonProperty("id") @Nullable UUID id,
            @JsonProperty("senderId") @NotNull UUID senderId,
            @JsonProperty("receiverId") @Nullable UUID receiverId,
            @JsonProperty("type") @NotNull String type,
            @JsonProperty("content") @NotNull Object content,
            @JsonProperty("metadata") @Nullable Map<String, Object> metadata,
            @JsonProperty("timestamp") @Nullable Instant timestamp,
            @JsonProperty("priority") @Nullable Priority priority) {
        
        this.id = id != null ? id : UUID.randomUUID();
        this.senderId = senderId;
        this.receiverId = receiverId;
        this.type = type;
        this.content = content;
        this.metadata = metadata != null ? new HashMap<>(metadata) : new HashMap<>();
        this.timestamp = timestamp != null ? timestamp : Instant.now();
        this.priority = priority != null ? priority : Priority.NORMAL;
    }
    
    // Builder pattern for easier message construction
    public static Builder builder() {
        return new Builder();
    }
    
    public static class Builder {
        private UUID senderId;
        private UUID receiverId;
        private String type;
        private Object content;
        private Map<String, Object> metadata = new HashMap<>();
        private Priority priority = Priority.NORMAL;
        
        public Builder from(@NotNull UUID senderId) {
            this.senderId = senderId;
            return this;
        }
        
        public Builder to(@NotNull UUID receiverId) {
            this.receiverId = receiverId;
            return this;
        }
        
        public Builder type(@NotNull String type) {
            this.type = type;
            return this;
        }
        
        public Builder content(@NotNull Object content) {
            this.content = content;
            return this;
        }
        
        public Builder metadata(@NotNull String key, @NotNull Object value) {
            this.metadata.put(key, value);
            return this;
        }
        
        public Builder metadata(@NotNull Map<String, Object> metadata) {
            this.metadata.putAll(metadata);
            return this;
        }
        
        public Builder priority(@NotNull Priority priority) {
            this.priority = priority;
            return this;
        }
        
        public Message build() {
            if (senderId == null) {
                throw new IllegalStateException("senderId is required");
            }
            if (type == null) {
                throw new IllegalStateException("type is required");
            }
            if (content == null) {
                throw new IllegalStateException("content is required");
            }
            
            return new Message(null, senderId, receiverId, type, content, 
                             metadata, null, priority);
        }
    }
    
    // Getters
    @NotNull
    public UUID getId() {
        return id;
    }
    
    @NotNull
    public UUID getSenderId() {
        return senderId;
    }
    
    @Nullable
    public UUID getReceiverId() {
        return receiverId;
    }
    
    @NotNull
    public String getType() {
        return type;
    }
    
    @NotNull
    public Object getContent() {
        return content;
    }
    
    @NotNull
    public Map<String, Object> getMetadata() {
        return new HashMap<>(metadata);
    }
    
    @Nullable
    public Object getMetadata(@NotNull String key) {
        return metadata.get(key);
    }
    
    @NotNull
    public Instant getTimestamp() {
        return timestamp;
    }
    
    @NotNull
    public Priority getPriority() {
        return priority;
    }
    
    /**
     * Creates a reply message to this message
     */
    @NotNull
    public Message.Builder createReply(@NotNull Object replyContent) {
        return Message.builder()
                .from(this.receiverId != null ? this.receiverId : UUID.randomUUID())
                .to(this.senderId)
                .type("reply")
                .content(replyContent)
                .metadata("replyTo", this.id.toString());
    }
    
    /**
     * Message priority levels
     */
    public enum Priority {
        LOW(1),
        NORMAL(5),
        HIGH(8),
        URGENT(10);
        
        private final int level;
        
        Priority(int level) {
            this.level = level;
        }
        
        public int getLevel() {
            return level;
        }
    }
    
    // Common message types
    public static final class Types {
        public static final String REQUEST = "request";
        public static final String RESPONSE = "response";
        public static final String NOTIFICATION = "notification";
        public static final String COMMAND = "command";
        public static final String EVENT = "event";
        public static final String ERROR = "error";
        public static final String HEARTBEAT = "heartbeat";
        
        private Types() {} // Utility class
    }
    
    @Override
    public String toString() {
        return String.format("Message{id=%s, from=%s, to=%s, type='%s', priority=%s, timestamp=%s}",
                id, senderId, receiverId, type, priority, timestamp);
    }
}
