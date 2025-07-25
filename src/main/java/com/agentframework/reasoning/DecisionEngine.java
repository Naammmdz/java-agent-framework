package com.agentframework.reasoning;

import com.agentframework.communication.Message;
import com.agentframework.tools.Tool;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * Interface for agent decision-making and reasoning capabilities.
 * The decision engine is responsible for planning and executing actions based on goals and stimuli.
 */
public interface DecisionEngine {
    
    /**
     * Processes a message and determines the appropriate response
     * @param message the message to process
     * @return response message
     */
    @NotNull
    Message processMessage(@NotNull Message message);
    
    /**
     * Plans and executes actions to achieve the given goal
     * @param goal the goal to achieve
     * @param availableTools tools that can be used
     * @return the result of goal execution
     */
    @NotNull
    Object executeGoal(@NotNull String goal, @NotNull List<Tool> availableTools);
    
    /**
     * Evaluates multiple options and chooses the best one
     * @param options list of options to evaluate
     * @param criteria evaluation criteria
     * @return the best option
     */
    @NotNull
    <T> T chooseBest(@NotNull List<T> options, @NotNull String criteria);
    
    /**
     * Creates a plan to achieve the given goal
     * @param goal the goal to plan for
     * @param availableTools tools that can be used
     * @return execution plan
     */
    @NotNull
    ExecutionPlan createPlan(@NotNull String goal, @NotNull List<Tool> availableTools);
    
    /**
     * Represents an execution plan with ordered steps
     */
    interface ExecutionPlan {
        
        /**
         * Gets the list of steps in this plan
         * @return ordered list of execution steps
         */
        @NotNull
        List<ExecutionStep> getSteps();
        
        /**
         * Gets the estimated completion time for this plan
         * @return estimated time in milliseconds
         */
        long getEstimatedTime();
        
        /**
         * Gets the confidence level for this plan (0.0 to 1.0)
         * @return confidence level
         */
        double getConfidence();
    }
    
    /**
     * Represents a single step in an execution plan
     */
    interface ExecutionStep {
        
        /**
         * Gets the tool to be used in this step
         * @return tool name
         */
        @NotNull
        String getToolName();
        
        /**
         * Gets the action description for this step
         * @return action description
         */
        @NotNull
        String getAction();
        
        /**
         * Gets the parameters for this step
         * @return step parameters
         */
        @NotNull
        java.util.Map<String, Object> getParameters();
        
        /**
         * Gets the expected outcome of this step
         * @return expected outcome description
         */
        @NotNull
        String getExpectedOutcome();
    }
}
