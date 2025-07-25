package com.agentframework.core;

import com.agentframework.behaviors.Behavior;
import com.agentframework.communication.Message;
import com.agentframework.tools.Tool;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

/**
 * Core Agent interface that defines the contract for all agents in the framework.
 * 
 * An Agent is an autonomous entity that can:
 * - Perceive its environment
 * - Process information and make decisions
 * - Execute actions through tools
 * - Communicate with other agents
 * - Learn and adapt over time
 */
public interface Agent {
    
    /**
     * Gets the unique identifier for this agent
     * @return agent's unique ID
     */
    @NotNull
    UUID getId();
    
    /**
     * Gets the human-readable name of this agent
     * @return agent's name
     */
    @NotNull
    String getName();
    
    /**
     * Gets the current state of the agent
     * @return current agent state
     */
    @NotNull
    AgentState getState();
    
    /**
     * Starts the agent and begins its execution cycle
     * @return CompletableFuture that completes when agent is fully started
     */
    @NotNull
    CompletableFuture<Void> start();
    
    /**
     * Stops the agent gracefully
     * @return CompletableFuture that completes when agent is fully stopped
     */
    @NotNull
    CompletableFuture<Void> stop();
    
    /**
     * Processes a message received from another agent or the environment
     * @param message the message to process
     * @return CompletableFuture containing the response, if any
     */
    @NotNull
    CompletableFuture<Message> processMessage(@NotNull Message message);
    
    /**
     * Executes a specific goal or task
     * @param goal the goal to achieve
     * @return CompletableFuture containing the result
     */
    @NotNull
    CompletableFuture<Object> executeGoal(@NotNull String goal);
    
    /**
     * Adds a behavior to this agent
     * @param behavior the behavior to add
     */
    void addBehavior(@NotNull Behavior behavior);
    
    /**
     * Removes a behavior from this agent
     * @param behavior the behavior to remove
     * @return true if the behavior was removed, false if it wasn't found
     */
    boolean removeBehavior(@NotNull Behavior behavior);
    
    /**
     * Gets all behaviors associated with this agent
     * @return list of agent behaviors
     */
    @NotNull
    List<Behavior> getBehaviors();
    
    /**
     * Adds a tool that this agent can use
     * @param tool the tool to add
     */
    void addTool(@NotNull Tool tool);
    
    /**
     * Gets all tools available to this agent
     * @return list of available tools
     */
    @NotNull
    List<Tool> getTools();
    
    /**
     * Gets the agent's knowledge base or memory
     * @return agent's memory/knowledge store
     */
    @NotNull
    AgentMemory getMemory();
    
    /**
     * Updates the agent's configuration
     * @param config new configuration
     */
    void updateConfig(@NotNull AgentConfig config);
    
    /**
     * Gets the current configuration
     * @return current agent configuration
     */
    @NotNull
    AgentConfig getConfig();
    
    /**
     * Agent lifecycle states
     */
    enum AgentState {
        CREATED,
        STARTING,
        RUNNING,
        PAUSED,
        STOPPING,
        STOPPED,
        ERROR
    }
}
