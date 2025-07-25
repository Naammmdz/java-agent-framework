# 📦 Java Agent Framework - Distribution Guide

This guide explains how to build and distribute the Java Agent Framework as a Maven dependency.

## 🎯 Distribution Options

### 1. **Local Installation** (Simplest)
Install to your local Maven repository for personal use.

### 2. **GitHub Packages** (Recommended for Teams)
Distribute via GitHub's Maven package registry.

### 3. **Maven Central** (Public Distribution)
Distribute via Maven Central for public consumption.

---

## 🏠 **Option 1: Local Installation**

### **Step 1: Build and Install Locally**
```bash
cd "D:\FullstackAI\acp\acp\examples\java-agent-framework"

# Clean and install to local repository
mvn clean install

# Or use the local profile
mvn clean deploy -Plocal
```

### **Step 2: Use in Your Projects**
Add to your project's `pom.xml`:
```xml
<dependency>
    <groupId>io.github.yourusername</groupId>
    <artifactId>java-agent-framework</artifactId>
    <version>1.0.0</version>
</dependency>
```

### **Step 3: Test the Dependency**
```java
// In your project
import com.agentframework.core.*;
import com.agentframework.ai.providers.OpenAIModel;

public class MyApp {
    public static void main(String[] args) {
        // Use the framework
        AIAgent agent = new AIAgent("MyBot", "api-key");
        System.out.println("Framework loaded successfully!");
    }
}
```

---

## 🐙 **Option 2: GitHub Packages**

### **Step 1: Setup GitHub Repository**
```bash
# Create a new repository on GitHub
# Clone and add your framework code
git clone https://github.com/yourusername/java-agent-framework.git
cd java-agent-framework

# Copy framework files
cp -r "D:\FullstackAI\acp\acp\examples\java-agent-framework\src" .
cp "D:\FullstackAI\acp\acp\examples\java-agent-framework\pom.xml" .
cp "D:\FullstackAI\acp\acp\examples\java-agent-framework\README.md" .
```

### **Step 2: Update pom.xml with Your Details**
```xml
<groupId>io.github.youractualusername</groupId>
<artifactId>java-agent-framework</artifactId>
<version>1.0.0</version>

<!-- Update URLs with your actual GitHub username -->
<url>https://github.com/youractualusername/java-agent-framework</url>
<scm>
    <connection>scm:git:git://github.com/youractualusername/java-agent-framework.git</connection>
    <developerConnection>scm:git:ssh://github.com:youractualusername/java-agent-framework.git</developerConnection>
    <url>https://github.com/youractualusername/java-agent-framework/tree/main</url>
</scm>
```

### **Step 3: Setup Maven Settings**
Create `~/.m2/settings.xml`:
```xml
<settings>
    <servers>
        <server>
            <id>github</id>
            <username>youractualusername</username>
            <password>your-github-personal-access-token</password>
        </server>
    </servers>
</settings>
```

### **Step 4: Deploy to GitHub Packages**
```bash
# Deploy to GitHub Packages
mvn clean deploy -Pgithub

# Or skip tests if needed
mvn clean deploy -Pgithub -DskipTests
```

### **Step 5: Use from GitHub Packages**
Users add this to their `pom.xml`:
```xml
<repositories>
    <repository>
        <id>github</id>
        <url>https://maven.pkg.github.com/youractualusername/java-agent-framework</url>
    </repository>
</repositories>

<dependency>
    <groupId>io.github.youractualusername</groupId>
    <artifactId>java-agent-framework</artifactId>
    <version>1.0.0</version>
</dependency>
```

---

## 🌍 **Option 3: Maven Central**

### **Step 1: Setup Sonatype Account**
1. Create account at [issues.sonatype.org](https://issues.sonatype.org)
2. Create a JIRA ticket to claim your `io.github.yourusername` groupId
3. Verify your GitHub account ownership

### **Step 2: Setup GPG Signing**
```bash
# Generate GPG key
gpg --gen-key

# List keys
gpg --list-secret-keys --keyid-format LONG

# Export public key
gpg --keyserver keyserver.ubuntu.com --send-keys YOUR_KEY_ID
```

### **Step 3: Deploy to Maven Central**
```bash
# Deploy to staging repository
mvn clean deploy

# Release (if auto-release is disabled)
mvn nexus-staging:release
```

---

## 🔧 **Build Commands Reference**

### **Basic Build Commands**
```bash
# Compile only
mvn compile

# Run tests
mvn test

# Package JAR
mvn package

# Install locally
mvn install

# Generate sources JAR
mvn source:jar

# Generate Javadoc JAR
mvn javadoc:jar

# Clean everything
mvn clean
```

### **Distribution Commands**
```bash
# Local installation
mvn clean install

# Deploy to local profile
mvn clean deploy -Plocal

# Deploy to GitHub Packages
mvn clean deploy -Pgithub

# Deploy to Maven Central (requires setup)
mvn clean deploy

# Skip GPG signing (for testing)
mvn clean deploy -Dgpg.skip=true

# Skip tests
mvn clean deploy -DskipTests
```

---

## 📄 **Generated Artifacts**

After building, you'll get these artifacts:
```
target/
├── java-agent-framework-1.0.0.jar           # Main JAR
├── java-agent-framework-1.0.0-sources.jar   # Source code JAR
├── java-agent-framework-1.0.0-javadoc.jar   # Documentation JAR
└── java-agent-framework-1.0.0.pom           # POM file
```

---

## 🚀 **Using the Framework Dependency**

### **Simple Usage Example**
```java
// pom.xml
<dependency>
    <groupId>io.github.yourusername</groupId>
    <artifactId>java-agent-framework</artifactId>
    <version>1.0.0</version>
</dependency>

// Java code
import com.agentframework.examples.AIAgent;

public class MyAgentApp {
    public static void main(String[] args) {
        AIAgent agent = new AIAgent("Assistant", System.getenv("OPENAI_API_KEY"));
        agent.start();
        
        // Use your agent...
    }
}
```

### **Advanced Usage Example**
```java
import com.agentframework.core.*;
import com.agentframework.ai.providers.OpenAIModel;
import com.agentframework.behaviors.Behavior;
import com.agentframework.tools.Tool;

public class CustomAgentApp {
    public static void main(String[] args) {
        // Create custom agent
        class MyCustomAgent extends BaseAgent {
            public MyCustomAgent() {
                super("CustomBot", AgentConfig.defaultConfig());
                
                // Add AI model
                var aiModel = new OpenAIModel(System.getenv("OPENAI_API_KEY"), "gpt-4");
                
                // Add custom behaviors and tools
                addBehavior(new MyCustomBehavior());
                addTool(new MyCustomTool());
                
                // Use AI decision engine
                this.decisionEngine = new AIDecisionEngine(aiModel);
            }
            
            @Override
            protected DecisionEngine createDecisionEngine() {
                return this.decisionEngine;
            }
        }
        
        // Use your custom agent
        MyCustomAgent agent = new MyCustomAgent();
        agent.start();
    }
}
```

---

## ✅ **Verification Steps**

### **1. Verify Local Installation**
```bash
# Check if installed in local repository
ls ~/.m2/repository/io/github/yourusername/java-agent-framework/1.0.0/

# Should see:
# - java-agent-framework-1.0.0.jar
# - java-agent-framework-1.0.0-sources.jar
# - java-agent-framework-1.0.0-javadoc.jar
# - java-agent-framework-1.0.0.pom
```

### **2. Test in New Project**
```bash
# Create test project
mvn archetype:generate -DgroupId=com.test -DartifactId=agent-test -DarchetypeArtifactId=maven-archetype-quickstart

# Add dependency and test
cd agent-test
# Edit pom.xml to add your framework dependency
mvn compile
```

### **3. Verify Documentation**
```bash
# Generate and view Javadocs
mvn javadoc:javadoc
open target/site/apidocs/index.html
```

---

## 🎯 **Quick Start for Different Scenarios**

### **For Personal Use (Local)**
```bash
mvn clean install
# Done! Use in your projects
```

### **For Team Use (GitHub)**
```bash
mvn clean deploy -Pgithub
# Share repository URL with team
```

### **For Public Use (Maven Central)**
```bash
# Setup Sonatype + GPG first, then:
mvn clean deploy
```

---

## 🔍 **Troubleshooting**

### **Common Issues**
1. **GPG signing fails**: Skip with `-Dgpg.skip=true` for testing
2. **Authentication fails**: Check `~/.m2/settings.xml`
3. **Upload fails**: Verify repository URLs and credentials
4. **Dependencies not found**: Run `mvn dependency:resolve`

### **Getting Help**
- Check Maven logs: `mvn -X deploy` (verbose mode)
- Verify settings: `mvn help:effective-settings`
- Test connection: `mvn dependency:resolve`

Your Java Agent Framework is now ready to be distributed as a professional Maven dependency! 🎉
