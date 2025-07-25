# Java Agent Framework

A modular, extensible framework for building AI agents in Java. This framework provides the core infrastructure and interfaces for creating AI agents while allowing you to bring your own AI provider (OpenAI, Anthropic, Azure OpenAI, local models, etc.).

## 🚀 Features

- **Modular Architecture**: Clean separation of concerns with interfaces for agents, behaviors, tools, and decision engines
- **Asynchronous Processing**: Full CompletableFuture support for non-blocking operations
- **Message-Based Communication**: Structured message passing between agents and external systems
- **Pluggable Behaviors**: Add and remove agent behaviors dynamically at runtime
- **Tool Integration**: Extensible tool system for agent capabilities
- **Memory Management**: Built-in memory system for storing agent experiences and knowledge
- **Configuration Management**: Flexible configuration system with builder patterns
- **Thread Safety**: Concurrent-safe implementations throughout
- **Comprehensive Logging**: SLF4J integration for observability

## 📁 Project Structure

```
src/main/java/com/agentframework/
├── core/                      # Core agent interfaces and implementations
│   ├── Agent.java            # Main agent interface
│   ├── BaseAgent.java        # Abstract base implementation
│   ├── AgentConfig.java      # Configuration management
│   ├── AgentMemory.java      # Memory interface
│   └── DefaultAgentMemory.java # Default memory implementation
├── communication/            # Message handling
│   └── Message.java         # Message class with builder pattern
├── behaviors/               # Agent behavior system
│   └── Behavior.java       # Behavior interface
├── tools/                   # Tool system for agent capabilities
│   └── Tool.java           # Tool interface with metadata
├── reasoning/              # Decision making and planning
│   └── DecisionEngine.java # Decision engine interface
└── examples/               # Example implementations
    ├── ChatAgent.java      # Sample conversational agent
    └── FrameworkDemo.java  # Demonstration program
```

## 🔧 Core Components

### 1. Agent Interface

The `Agent` interface defines the contract for all agents:

```java
public interface Agent {
    CompletableFuture<Void> start();
    CompletableFuture<Void> stop();
    CompletableFuture<Message> processMessage(Message message);
    CompletableFuture<Object> executeGoal(String goal);
    void addBehavior(Behavior behavior);
    void addTool(Tool tool);
    // ... more methods
}
```

### 2. Message System

Structured communication with builder pattern:

```java
Message message = Message.builder()
    .from(senderId)
    .to(receiverId)
    .type("request")
    .content("Hello, agent!")
    .priority(Message.Priority.HIGH)
    .build();
```

### 3. Behaviors

Define how agents respond to stimuli:

```java
public class GreetingBehavior implements Behavior {
    public boolean canHandle(Message message) {
        return message.getContent().toString().contains("hello");
    }
    
    public Message process(Message message) {
        return message.createReply("Hello! How can I help?").build();
    }
}
```

### 4. Tools

Extend agent capabilities:

```java
public class CalculatorTool implements Tool {
    public CompletableFuture<ToolResult> execute(Map<String, Object> parameters) {
        // Perform calculation
        return CompletableFuture.completedFuture(ToolResult.success(result));
    }
}
```

## 🚀 Quick Start

### 1. Add Dependencies

Include in your `pom.xml`:

```xml
<dependencies>
    <!-- See pom.xml for complete dependency list -->
    <dependency>
        <groupId>com.fasterxml.jackson.core</groupId>
        <artifactId>jackson-databind</artifactId>
        <version>2.15.2</version>
    </dependency>
    <!-- ... other dependencies -->
</dependencies>
```

### 2. Create Your First Agent

```java
public class MyAgent extends BaseAgent {
    
    public MyAgent(String name) {
        super(name, AgentConfig.defaultConfig());
        
        // Add behaviors
        addBehavior(new GreetingBehavior());
        
        // Add tools
        addTool(new CalculatorTool());
    }
    
    @Override
    protected DecisionEngine createDecisionEngine() {
        return new MyDecisionEngine();
    }
}
```

### 3. Start and Use the Agent

```java
// Create agent
MyAgent agent = new MyAgent("MyBot");

// Start agent
agent.start().get();

// Send message
Message message = Message.builder()
    .from(UUID.randomUUID())
    .to(agent.getId())
    .type("request")
    .content("Hello!")
    .build();

Message response = agent.processMessage(message).get();
System.out.println("Agent replied: " + response.getContent());

// Execute goal
Object result = agent.executeGoal("Calculate 2 + 2").get();
System.out.println("Result: " + result);

// Stop agent
agent.stop().get();
```

## 🎯 Running the Demo

Compile and run the demonstration:

```bash
# Compile
mvn compile

# Run demo
mvn exec:java -Dexec.mainClass="com.agentframework.examples.FrameworkDemo"
```

The demo includes:
- Basic message interaction
- Goal execution
- Tool usage demonstration
- Interactive chat mode

## 🏗️ Architecture Patterns

### 1. Template Method Pattern
`BaseAgent` provides a template for agent lifecycle with customizable decision engines.

### 2. Strategy Pattern
Different decision engines can be plugged in for different reasoning strategies.

### 3. Observer Pattern
Behaviors observe and react to messages and events.

### 4. Builder Pattern
Used extensively for configuration and message creation.

### 5. Factory Pattern
Can be extended for creating different types of agents and tools.

## 📈 Extending the Framework

### Custom Behaviors

```java
public class CustomBehavior implements Behavior {
    @Override
    public boolean canHandle(Message message) {
        // Your logic here
        return true;
    }
    
    @Override
    public Message process(Message message) {
        // Your processing logic
        return message.createReply("Custom response").build();
    }
}
```

### Custom Tools

```java
public class APITool implements Tool {
    @Override
    public CompletableFuture<ToolResult> execute(Map<String, Object> parameters) {
        return CompletableFuture.supplyAsync(() -> {
            // Make API call
            return ToolResult.success(response);
        });
    }
}
```

### Custom Decision Engines

```java
public class AIDecisionEngine implements DecisionEngine {
    @Override
    public Object executeGoal(String goal, List<Tool> tools) {
        // Integrate with AI models for intelligent planning
        return planAndExecute(goal, tools);
    }
}
```

## 🔒 Thread Safety

The framework is designed with thread safety in mind:
- `CopyOnWriteArrayList` for collections that change infrequently
- `ConcurrentHashMap` for key-value storage
- `AtomicReference` for state management
- Immutable message objects
- Thread-safe executor services

## 📊 Performance Considerations

- Async operations prevent blocking
- Configurable thread pools for different workloads
- Memory-efficient message handling
- Lazy initialization of components
- Resource cleanup on agent shutdown

## 🧪 Testing

The framework is designed for testability:

```java
@Test
public void testAgentResponse() {
    ChatAgent agent = new ChatAgent("TestBot");
    agent.start().get();
    
    Message message = Message.builder()
        .from(UUID.randomUUID())
        .to(agent.getId())
        .type("request")
        .content("Hello")
        .build();
    
    Message response = agent.processMessage(message).get();
    assertTrue(response.getContent().toString().contains("Hello"));
    
    agent.stop().get();
}
```

## 🤖 AI Integration

The framework now includes comprehensive AI model integration:

### Supported AI Providers
- **Provider Agnostic**: Bring your own AI provider by implementing the `AIModel` interface
- **Popular Providers**: OpenAI, Anthropic, Azure OpenAI, Google PaLM, local models, etc.
- **Easy Integration**: Simple interface to connect any AI service

### AI Features
- **Intelligent Conversation**: Natural language understanding and generation
- **AI-Powered Decision Engine**: Uses LLMs for planning and reasoning
- **Smart Tools**: AI-enhanced calculator, search, and writing tools
- **Function Calling**: Integration with OpenAI function calling API
- **Configurable Parameters**: Temperature, tokens, and other AI parameters

### Quick Start with AI

```java
// First, implement your AI provider
AIModel myAIModel = new MyAIProvider("your-api-key", "model-name");

// Create AI-powered agent with your provider
AIAgent agent = new AIAgent("Assistant", myAIModel);
agent.start().get();

// Chat with the agent
Message message = Message.builder()
    .from(userId)
    .to(agent.getId())
    .type("request")
    .content("Explain quantum computing")
    .build();

Message response = agent.processMessage(message).get();
System.out.println("AI Response: " + response.getContent());

// Execute complex goals
Object result = agent.executeGoal("Plan a healthy meal for tomorrow").get();
```

### Implementing Your AI Provider

```java
// Implement the AIModel interface with your chosen provider
public class MyAIProvider implements AIModel {
    private final String apiKey;
    private final String modelName;
    
    public MyAIProvider(String apiKey, String modelName) {
        this.apiKey = apiKey;
        this.modelName = modelName;
    }
    
    @Override
    public CompletableFuture<AIResponse> generateResponse(String prompt) {
        return CompletableFuture.supplyAsync(() -> {
            // Implement using your AI provider's client
            // Return new AIResponse(...)
        });
    }
    
    // Implement other required methods...
}
```

### Custom AI Models

```java
// Implement custom AI provider
public class CustomAIModel implements AIModel {
    @Override
    public CompletableFuture<AIResponse> generateResponse(String prompt) {
        // Your implementation here
    }
    // ... other methods
}

// Use with agent
Agent agent = new AgentBuilder()
    .withName("CustomBot")
    .withAIModel(new CustomAIModel())
    .build();
```

## 🚀 Next Steps

1. **✅ AI Integration**: ✨ **Now Available!** Connect to language models for intelligent decision making
2. **Persistence Layer**: Add database support for agent memory
3. **Network Communication**: Implement distributed agent systems
4. **Web Interface**: Create REST APIs for agent interaction
5. **Monitoring**: Add metrics and health checks
6. **Security**: Implement authentication and authorization

## 📖 API Documentation

For detailed API documentation, generate Javadocs:

```bash
mvn javadoc:javadoc
```

## 🤝 Contributing

This framework provides a solid foundation for building production-ready agent systems. Extend it based on your specific requirements!

## 📄 License

This framework is provided as an example for educational and development purposes.
