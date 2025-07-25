package com.agentframework.behaviors;

import com.agentframework.communication.Message;
import com.agentframework.core.Agent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Interface for agent behaviors that define how agents respond to stimuli.
 * Behaviors can be reactive (responding to messages) or proactive (executing periodically).
 */
public interface Behavior {
    
    /**
     * Gets the unique name of this behavior
     * @return behavior name
     */
    @NotNull
    String getName();
    
    /**
     * Gets the priority of this behavior (higher numbers = higher priority)
     * @return behavior priority
     */
    int getPriority();
    
    /**
     * Initializes this behavior with the given agent
     * @param agent the agent this behavior belongs to
     */
    void initialize(@NotNull Agent agent);
    
    /**
     * Checks if this behavior can handle the given message
     * @param message the message to check
     * @return true if this behavior can handle the message
     */
    boolean canHandle(@NotNull Message message);
    
    /**
     * Processes a message and optionally returns a response
     * @param message the message to process
     * @return response message, or null if no response is needed
     */
    @Nullable
    Message process(@NotNull Message message);
    
    /**
     * Executes proactive behavior (called periodically by the agent)
     */
    void execute();
    
    /**
     * Cleans up resources when the behavior is removed or agent stops
     */
    void cleanup();
    
    /**
     * Checks if this behavior is currently active
     * @return true if active, false otherwise
     */
    boolean isActive();
    
    /**
     * Sets the active state of this behavior
     * @param active whether the behavior should be active
     */
    void setActive(boolean active);
}
