package com.agentframework.reasoning;

import com.agentframework.ai.AIModel;
import com.agentframework.communication.Message;
import com.agentframework.tools.Tool;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

/**
 * AI-powered decision engine that uses language models for reasoning and planning.
 * This engine can understand natural language goals and create execution plans.
 */
public class AIDecisionEngine implements DecisionEngine {
    
    private static final Logger logger = LoggerFactory.getLogger(AIDecisionEngine.class);
    
    private final AIModel aiModel;
    private final ObjectMapper objectMapper;
    private final String systemPrompt;
    
    public AIDecisionEngine(@NotNull AIModel aiModel) {
        this(aiModel, createDefaultSystemPrompt());
    }
    
    public AIDecisionEngine(@NotNull AIModel aiModel, @NotNull String systemPrompt) {
        this.aiModel = aiModel;
        this.objectMapper = new ObjectMapper();
        this.systemPrompt = systemPrompt;
    }
    
    @NotNull
    @Override
    public Message processMessage(@NotNull Message message) {
        try {
            String prompt = String.format(
                "Process this message and generate an appropriate response:\n" +
                "Message Type: %s\n" +
                "Content: %s\n" +
                "Sender: %s\n\n" +
                "Generate a helpful and contextual response.",
                message.getType(),
                message.getContent(),
                message.getSenderId()
            );
            
            AIModel.AIResponse response = aiModel.generateResponse(prompt).get();
            return message.createReply(response.getContent()).build();
            
        } catch (Exception e) {
            logger.error("Error processing message with AI", e);
            return message.createReply("I'm sorry, I encountered an error processing your message.").build();
        }
    }
    
    @NotNull
    @Override
    public Object executeGoal(@NotNull String goal, @NotNull List<Tool> availableTools) {
        try {
            logger.info("AI Decision Engine executing goal: {}", goal);
            
            // Create execution plan using AI
            ExecutionPlan plan = createPlan(goal, availableTools);
            
            // Execute the plan
            Object result = executePlan(plan, availableTools);
            
            logger.info("Goal execution completed: {}", result);
            return result;
            
        } catch (Exception e) {
            logger.error("Error executing goal with AI", e);
            return "Failed to execute goal: " + e.getMessage();
        }
    }
    
    @NotNull
    @Override
    public <T> T chooseBest(@NotNull List<T> options, @NotNull String criteria) {
        if (options.isEmpty()) {
            throw new IllegalArgumentException("No options provided");
        }
        
        if (options.size() == 1) {
            return options.get(0);
        }
        
        try {
            String optionsText = options.stream()
                .map(Object::toString)
                .map(s -> "- " + s)
                .collect(Collectors.joining("\n"));
            
            String prompt = String.format(
                "Choose the best option based on the criteria: %s\n\n" +
                "Options:\n%s\n\n" +
                "Return only the index (0-based) of the best option as a number.",
                criteria,
                optionsText
            );
            
            AIModel.AIResponse response = aiModel.generateResponse(prompt).get();
            
            try {
                int index = Integer.parseInt(response.getContent().trim());
                if (index >= 0 && index < options.size()) {
                    return options.get(index);
                }
            } catch (NumberFormatException e) {
                logger.warn("AI returned non-numeric choice, using first option");
            }
            
            return options.get(0);
            
        } catch (Exception e) {
            logger.error("Error choosing best option with AI", e);
            return options.get(0);
        }
    }
    
    @NotNull
    @Override
    public ExecutionPlan createPlan(@NotNull String goal, @NotNull List<Tool> availableTools) {
        try {
            String toolsDescription = availableTools.stream()
                .map(tool -> String.format("- %s: %s", tool.getName(), tool.getDescription()))
                .collect(Collectors.joining("\n"));
            
            String prompt = String.format(
                "Create an execution plan to achieve this goal: %s\n\n" +
                "Available tools:\n%s\n\n" +
                "Return a JSON plan with this structure:\n" +
                "{\n" +
                "  \"steps\": [\n" +
                "    {\n" +
                "      \"toolName\": \"tool_name\",\n" +
                "      \"action\": \"description of action\",\n" +
                "      \"parameters\": {\"param1\": \"value1\"},\n" +
                "      \"expectedOutcome\": \"what should happen\"\n" +
                "    }\n" +
                "  ],\n" +
                "  \"estimatedTime\": 5000,\n" +
                "  \"confidence\": 0.85\n" +
                "}",
                goal,
                toolsDescription
            );
            
            List<AIModel.ChatMessage> messages = List.of(
                AIModel.ChatMessage.system(systemPrompt),
                AIModel.ChatMessage.user(prompt)
            );
            
            AIModel.AIParameters params = AIModel.AIParameters.builder()
                .temperature(0.3) // Lower temperature for more deterministic planning
                .maxTokens(1000)
                .build();
            
            AIModel.AIResponse response = aiModel.continueConversation(messages, params).get();
            
            return parsePlanFromJson(response.getContent(), availableTools);
            
        } catch (Exception e) {
            logger.error("Error creating plan with AI", e);
            return createFallbackPlan(goal, availableTools);
        }
    }
    
    /**
     * Executes the given plan using available tools
     */
    private Object executePlan(ExecutionPlan plan, List<Tool> availableTools) {
        StringBuilder results = new StringBuilder();
        Map<String, Tool> toolMap = availableTools.stream()
            .collect(Collectors.toMap(Tool::getName, tool -> tool));
        
        for (ExecutionStep step : plan.getSteps()) {
            try {
                Tool tool = toolMap.get(step.getToolName());
                if (tool == null) {
                    results.append("Tool not found: ").append(step.getToolName()).append("\n");
                    continue;
                }
                
                logger.info("Executing step: {} with tool: {}", step.getAction(), step.getToolName());
                
                Tool.ToolResult result = tool.execute(step.getParameters()).get();
                
                if (result.isSuccess()) {
                    results.append("✓ ").append(step.getAction())
                           .append(" -> ").append(result.getData()).append("\n");
                } else {
                    results.append("✗ ").append(step.getAction())
                           .append(" -> ERROR: ").append(result.getError()).append("\n");
                }
                
            } catch (Exception e) {
                logger.error("Error executing step: " + step.getAction(), e);
                results.append("✗ ").append(step.getAction())
                       .append(" -> EXCEPTION: ").append(e.getMessage()).append("\n");
            }
        }
        
        return results.toString();
    }
    
    /**
     * Parses an execution plan from JSON response
     */
    private ExecutionPlan parsePlanFromJson(String jsonContent, List<Tool> availableTools) {
        try {
            // Try to extract JSON from the response (in case there's extra text)
            String json = extractJson(jsonContent);
            
            JsonNode planNode = objectMapper.readTree(json);
            
            List<ExecutionStep> steps = new ArrayList<>();
            JsonNode stepsNode = planNode.get("steps");
            
            if (stepsNode != null && stepsNode.isArray()) {
                for (JsonNode stepNode : stepsNode) {
                    String toolName = stepNode.get("toolName").asText();
                    String action = stepNode.get("action").asText();
                    String expectedOutcome = stepNode.get("expectedOutcome").asText();
                    
                    Map<String, Object> parameters = new HashMap<>();
                    JsonNode parametersNode = stepNode.get("parameters");
                    if (parametersNode != null) {
                        parametersNode.fields().forEachRemaining(entry -> {
                            parameters.put(entry.getKey(), entry.getValue().asText());
                        });
                    }
                    
                    steps.add(new ExecutionStepImpl(toolName, action, parameters, expectedOutcome));
                }
            }
            
            long estimatedTime = planNode.has("estimatedTime") ? 
                planNode.get("estimatedTime").asLong() : 5000;
            double confidence = planNode.has("confidence") ? 
                planNode.get("confidence").asDouble() : 0.7;
            
            return new ExecutionPlanImpl(steps, estimatedTime, confidence);
            
        } catch (Exception e) {
            logger.warn("Failed to parse AI plan JSON, using fallback", e);
            return createFallbackPlan("Parse JSON plan", availableTools);
        }
    }
    
    /**
     * Extracts JSON from AI response that might contain additional text
     */
    private String extractJson(String content) {
        int start = content.indexOf('{');
        int end = content.lastIndexOf('}');
        
        if (start >= 0 && end >= 0 && end > start) {
            return content.substring(start, end + 1);
        }
        
        return content;
    }
    
    /**
     * Creates a fallback plan when AI planning fails
     */
    private ExecutionPlan createFallbackPlan(String goal, List<Tool> availableTools) {
        List<ExecutionStep> steps = new ArrayList<>();
        
        if (!availableTools.isEmpty()) {
            Tool firstTool = availableTools.get(0);
            steps.add(new ExecutionStepImpl(
                firstTool.getName(),
                "Execute goal: " + goal,
                Map.of(),
                "Complete the requested task"
            ));
        }
        
        return new ExecutionPlanImpl(steps, 5000, 0.5);
    }
    
    /**
     * Creates the default system prompt for the AI decision engine
     */
    private static String createDefaultSystemPrompt() {
        return """
            You are an AI decision engine for an intelligent agent system. Your role is to:
            
            1. Process messages and generate appropriate responses
            2. Create execution plans to achieve goals using available tools
            3. Make decisions based on given criteria
            4. Think step-by-step and be practical in your approach
            
            When creating execution plans:
            - Break down complex goals into simple, actionable steps
            - Use available tools effectively
            - Provide realistic time estimates
            - Be specific about expected outcomes
            
            Always respond in a helpful, clear, and actionable manner.
            """;
    }
    
    /**
     * Implementation of ExecutionPlan
     */
    private static class ExecutionPlanImpl implements ExecutionPlan {
        private final List<ExecutionStep> steps;
        private final long estimatedTime;
        private final double confidence;
        
        public ExecutionPlanImpl(List<ExecutionStep> steps, long estimatedTime, double confidence) {
            this.steps = List.copyOf(steps);
            this.estimatedTime = estimatedTime;
            this.confidence = confidence;
        }
        
        @NotNull
        @Override
        public List<ExecutionStep> getSteps() {
            return steps;
        }
        
        @Override
        public long getEstimatedTime() {
            return estimatedTime;
        }
        
        @Override
        public double getConfidence() {
            return confidence;
        }
    }
    
    /**
     * Implementation of ExecutionStep
     */
    private static class ExecutionStepImpl implements ExecutionStep {
        private final String toolName;
        private final String action;
        private final Map<String, Object> parameters;
        private final String expectedOutcome;
        
        public ExecutionStepImpl(String toolName, String action, 
                                Map<String, Object> parameters, String expectedOutcome) {
            this.toolName = toolName;
            this.action = action;
            this.parameters = Map.copyOf(parameters);
            this.expectedOutcome = expectedOutcome;
        }
        
        @NotNull
        @Override
        public String getToolName() {
            return toolName;
        }
        
        @NotNull
        @Override
        public String getAction() {
            return action;
        }
        
        @NotNull
        @Override
        public Map<String, Object> getParameters() {
            return parameters;
        }
        
        @NotNull
        @Override
        public String getExpectedOutcome() {
            return expectedOutcome;
        }
    }
}
