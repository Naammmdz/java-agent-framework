package com.agentframework.core;

import com.agentframework.behaviors.Behavior;
import com.agentframework.communication.Message;
import com.agentframework.reasoning.DecisionEngine;
import com.agentframework.tools.Tool;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Abstract base implementation of the Agent interface providing common functionality.
 * Concrete agent implementations should extend this class.
 */
public abstract class BaseAgent implements Agent {
    
    private static final Logger logger = LoggerFactory.getLogger(BaseAgent.class);
    
    protected final UUID id;
    protected final String name;
    protected final AtomicReference<AgentState> state;
    protected final List<Behavior> behaviors;
    protected final List<Tool> tools;
    protected final AgentMemory memory;
    protected final DecisionEngine decisionEngine;
    protected final ExecutorService executorService;
    protected volatile AgentConfig config;
    
    protected BaseAgent(@NotNull String name, @NotNull AgentConfig config) {
        this.id = UUID.randomUUID();
        this.name = name;
        this.config = config;
        this.state = new AtomicReference<>(AgentState.CREATED);
        this.behaviors = new CopyOnWriteArrayList<>();
        this.tools = new CopyOnWriteArrayList<>();
        this.memory = new DefaultAgentMemory();
        this.decisionEngine = createDecisionEngine();
        this.executorService = Executors.newCachedThreadPool(r -> {
            Thread t = new Thread(r, "Agent-" + name + "-" + id);
            t.setDaemon(true);
            return t;
        });
        
        logger.info("Created agent: {} with ID: {}", name, id);
    }
    
    /**
     * Template method for creating the decision engine.
     * Subclasses can override to provide custom decision engines.
     */
    protected abstract DecisionEngine createDecisionEngine();
    
    @NotNull
    @Override
    public UUID getId() {
        return id;
    }
    
    @NotNull
    @Override
    public String getName() {
        return name;
    }
    
    @NotNull
    @Override
    public AgentState getState() {
        return state.get();
    }
    
    @NotNull
    @Override
    public CompletableFuture<Void> start() {
        return CompletableFuture.runAsync(() -> {
            if (!state.compareAndSet(AgentState.CREATED, AgentState.STARTING) &&
                !state.compareAndSet(AgentState.STOPPED, AgentState.STARTING)) {
                throw new IllegalStateException("Cannot start agent in state: " + state.get());
            }
            
            try {
                logger.info("Starting agent: {}", name);
                
                // Initialize behaviors
                for (Behavior behavior : behaviors) {
                    behavior.initialize(this);
                }
                
                // Perform custom startup logic
                onStart();
                
                state.set(AgentState.RUNNING);
                logger.info("Agent started successfully: {}", name);
                
                // Start the main execution loop
                startExecutionLoop();
                
            } catch (Exception e) {
                state.set(AgentState.ERROR);
                logger.error("Failed to start agent: " + name, e);
                throw new RuntimeException("Failed to start agent", e);
            }
        }, executorService);
    }
    
    @NotNull
    @Override
    public CompletableFuture<Void> stop() {
        return CompletableFuture.runAsync(() -> {
            if (!state.compareAndSet(AgentState.RUNNING, AgentState.STOPPING) &&
                !state.compareAndSet(AgentState.PAUSED, AgentState.STOPPING)) {
                return; // Already stopped or stopping
            }
            
            try {
                logger.info("Stopping agent: {}", name);
                
                // Stop behaviors
                for (Behavior behavior : behaviors) {
                    behavior.cleanup();
                }
                
                // Perform custom shutdown logic
                onStop();
                
                state.set(AgentState.STOPPED);
                logger.info("Agent stopped successfully: {}", name);
                
            } catch (Exception e) {
                state.set(AgentState.ERROR);
                logger.error("Error stopping agent: " + name, e);
                throw new RuntimeException("Failed to stop agent", e);
            } finally {
                executorService.shutdown();
            }
        }, executorService);
    }
    
    @NotNull
    @Override
    public CompletableFuture<Message> processMessage(@NotNull Message message) {
        return CompletableFuture.supplyAsync(() -> {
            if (state.get() != AgentState.RUNNING) {
                throw new IllegalStateException("Agent is not running, current state: " + state.get());
            }
            
            logger.debug("Processing message: {} from: {}", message.getType(), message.getSenderId());
            
            try {
                // Store message in memory
                memory.storeMessage(message);
                
                // Process through behaviors first
                for (Behavior behavior : behaviors) {
                    if (behavior.canHandle(message)) {
                        Message response = behavior.process(message);
                        if (response != null) {
                            return response;
                        }
                    }
                }
                
                // If no behavior handled it, use decision engine
                return decisionEngine.processMessage(message);
                
            } catch (Exception e) {
                logger.error("Error processing message", e);
                return message.createReply("Error processing message: " + e.getMessage()).build();
            }
        }, executorService);
    }
    
    @NotNull
    @Override
    public CompletableFuture<Object> executeGoal(@NotNull String goal) {
        return CompletableFuture.supplyAsync(() -> {
            if (state.get() != AgentState.RUNNING) {
                throw new IllegalStateException("Agent is not running, current state: " + state.get());
            }
            
            logger.info("Executing goal: {}", goal);
            
            try {
                // Store goal in memory
                memory.storeGoal(goal);
                
                // Use decision engine to plan and execute
                return decisionEngine.executeGoal(goal, tools);
                
            } catch (Exception e) {
                logger.error("Error executing goal: " + goal, e);
                throw new RuntimeException("Failed to execute goal", e);
            }
        }, executorService);
    }
    
    @Override
    public void addBehavior(@NotNull Behavior behavior) {
        behaviors.add(behavior);
        if (state.get() == AgentState.RUNNING) {
            behavior.initialize(this);
        }
        logger.debug("Added behavior: {} to agent: {}", behavior.getClass().getSimpleName(), name);
    }
    
    @Override
    public boolean removeBehavior(@NotNull Behavior behavior) {
        boolean removed = behaviors.remove(behavior);
        if (removed) {
            behavior.cleanup();
            logger.debug("Removed behavior: {} from agent: {}", behavior.getClass().getSimpleName(), name);
        }
        return removed;
    }
    
    @NotNull
    @Override
    public List<Behavior> getBehaviors() {
        return List.copyOf(behaviors);
    }
    
    @Override
    public void addTool(@NotNull Tool tool) {
        tools.add(tool);
        logger.debug("Added tool: {} to agent: {}", tool.getName(), name);
    }
    
    @NotNull
    @Override
    public List<Tool> getTools() {
        return List.copyOf(tools);
    }
    
    @NotNull
    @Override
    public AgentMemory getMemory() {
        return memory;
    }
    
    @Override
    public void updateConfig(@NotNull AgentConfig config) {
        this.config = config;
        logger.info("Updated configuration for agent: {}", name);
    }
    
    @NotNull
    @Override
    public AgentConfig getConfig() {
        return config;
    }
    
    /**
     * Template method called during agent startup.
     * Subclasses can override to provide custom initialization logic.
     */
    protected void onStart() {
        // Default implementation does nothing
    }
    
    /**
     * Template method called during agent shutdown.
     * Subclasses can override to provide custom cleanup logic.
     */
    protected void onStop() {
        // Default implementation does nothing
    }
    
    /**
     * Starts the main execution loop for proactive behaviors.
     */
    private void startExecutionLoop() {
        CompletableFuture.runAsync(() -> {
            while (state.get() == AgentState.RUNNING) {
                try {
                    // Execute proactive behaviors
                    for (Behavior behavior : behaviors) {
                        if (state.get() != AgentState.RUNNING) break;
                        behavior.execute();
                    }
                    
                    // Sleep for a short interval
                    Thread.sleep(config.getExecutionInterval());
                    
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                } catch (Exception e) {
                    logger.error("Error in execution loop", e);
                }
            }
        }, executorService);
    }
}
