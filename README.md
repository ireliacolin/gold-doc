# Gold Document Management System

A Spring Boot-based document management system for the gold industry.

## Features

- Document CRUD operations
- Document status tracking (DRAFT, REVIEW, APPROVED, ARCHIVED)
- Search functionality
- Document statistics
- RESTful API with OpenAPI documentation
- H2 in-memory database for development
- JPA with Hibernate
- Spring Security (basic authentication)
- Comprehensive test coverage

## Technology Stack

- Java 17
- Spring Boot 3.2.0
- Spring Data JPA
- Spring Security
- H2 Database (development)
- PostgreSQL (production-ready)
- Lombok
- SpringDoc OpenAPI 3
- JUnit 5 & Mockito
- Maven
- **Netty 4.1** (WebSocket server/client)

## Prerequisites

- Java 17 or higher
- Maven 3.8+
- Git

## Getting Started

### 1. Clone the repository
```bash
git clone https://github.com/ireliacolin/gold-doc.git
cd gold-doc
```

### 2. Build the project
```bash
mvn clean install
```

### 3. Run the application
```bash
mvn spring-boot:run
```

### 4. Access the application
- Application: http://localhost:8080/api
- H2 Console: http://localhost:8080/api/h2-console
- Swagger UI: http://localhost:8080/api/swagger-ui.html
- API Docs: http://localhost:8080/api/api-docs
- **WebSocket Server**: ws://localhost:8081/ws
- **WebSocket Status**: http://localhost:8080/api/websocket/status

Default credentials:
- Username: admin
- Password: admin123

## API Endpoints

### Document Management
- `GET /api/documents` - Get all documents
- `GET /api/documents/{id}` - Get document by ID
- `POST /api/documents` - Create a new document
- `PUT /api/documents/{id}` - Update a document
- `DELETE /api/documents/{id}` - Delete a document
- `GET /api/documents/status/{status}` - Get documents by status
- `GET /api/documents/search?keyword={keyword}` - Search documents
- `GET /api/documents/type/{type}` - Get documents by type
- `GET /api/documents/stats` - Get document statistics

### Health Check
- `GET /api/documents/health` - Document service health check
- `GET /api/websocket/health` - WebSocket server health check

### WebSocket Management
- `GET /api/websocket/status` - Get WebSocket server status and connected users
- `GET /api/websocket/info` - Get WebSocket connection information

## Project Structure

```
src/main/java/com/golddoc/
├── GoldDocApplication.java          # Main application class
├── controller/
│   ├── DocumentController.java      # REST controller
│   └── WebSocketController.java     # WebSocket management API
├── model/
│   ├── Document.java                # Entity class
│   └── websocket/
│       └── WebSocketMessage.java    # WebSocket message model
├── repository/
│   └── DocumentRepository.java      # JPA repository
├── service/
│   └── DocumentService.java         # Business logic
├── websocket/
│   ├── NettyWebSocketServer.java    # WebSocket server
│   ├── handler/
│   │   ├── WebSocketServerHandler.java
│   │   └── WebSocketServerInitializer.java
│   └── client/
│       ├── WebSocketClient.java
│       └── WebSocketClientHandler.java

src/main/resources/
├── application.yml                  # Application configuration
└── (other resources)

src/test/java/com/golddoc/
├── GoldDocApplicationTests.java     # Main test class
├── service/
│   └── DocumentServiceTest.java     # Service unit tests
└── websocket/
    └── WebSocketTestClient.java     # WebSocket test client
```

## Testing

### Run all tests
```bash
mvn test
```

### Run tests with coverage report
```bash
mvn clean test jacoco:report
```

Coverage report will be generated at: `target/site/jacoco/index.html`

### Test coverage includes:
- Unit tests for service layer
- Integration tests for controller layer
- Repository tests

## Database Configuration

### Development (H2)
- URL: jdbc:h2:mem:golddocdb
- Username: sa
- Password: (empty)
- Console: http://localhost:8080/api/h2-console

### Production (PostgreSQL)
Update `application.yml` with PostgreSQL configuration:
```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/golddocdb
    username: your_username
    password: your_password
  jpa:
    database-platform: org.hibernate.dialect.PostgreSQLDialect
```

## Security

Basic authentication is configured with a single user:
- Username: admin
- Password: admin123

For production use, configure proper authentication:
1. Enable JWT or OAuth2
2. Implement user roles and permissions
3. Use database-backed user management

## Deployment

### Build JAR file
```bash
mvn clean package
```

### Run JAR file
```bash
java -jar target/gold-doc-1.0.0.jar
```

### Docker (optional)
```dockerfile
FROM openjdk:17-jdk-slim
COPY target/gold-doc-1.0.0.jar app.jar
ENTRYPOINT ["java", "-jar", "/app.jar"]
```

## Development Guidelines

### Code Style
- Follow Java naming conventions
- Use meaningful variable and method names
- Add Javadoc comments for public APIs
- Keep methods small and focused

### Testing
- Write tests for all business logic
- Aim for >80% code coverage
- Use meaningful test names
- Follow AAA pattern (Arrange, Act, Assert)

### Git Commit Messages
- Use conventional commit messages
- Reference issue numbers when applicable
- Keep commits focused and atomic

## License

This project is licensed under the MIT License - see the LICENSE file for details.

## WebSocket Usage

### WebSocket Server
The application includes a Netty-based WebSocket server running on port 8081.

#### Connection URL
```
ws://localhost:8081/ws
```

#### Message Format
```json
{
  "type": "MESSAGE",
  "sender": "user-id",
  "content": "Hello World",
  "room": "general"
}
```

#### Message Types
- `CONNECT` - Connection established
- `MESSAGE` - Regular message (can specify room)
- `BROADCAST` - Broadcast to all connected users
- `NOTIFICATION` - System notification
- `ERROR` - Error message
- `DISCONNECT` - Connection closed

### WebSocket Client Example

```java
// Create client
WebSocketClient client = new WebSocketClient("ws://localhost:8081/ws", "user123");

// Set message handler
client.setMessageConsumer(message -> {
    System.out.printf("[%s] %s: %s%n", 
        message.getTimestamp(), 
        message.getSender(),
        message.getContent());
});

// Connect
client.connect();

// Send message
client.sendTextMessage("Hello from user123", "general");

// Send broadcast
client.sendBroadcast("This is a broadcast message");

// Disconnect
client.disconnect();
```

### Running Test Client
```bash
# Run test client
mvn compile exec:java -Dexec.mainClass="com.golddoc.websocket.WebSocketTestClient" -Dexec.args="test-user-1"

# Or compile and run manually
javac -cp "target/classes:target/test-classes:$(find ~/.m2/repository -name '*.jar' | tr '\n' ':')" \
    src/test/java/com/golddoc/websocket/WebSocketTestClient.java
java -cp "target/classes:target/test-classes:$(find ~/.m2/repository -name '*.jar' | tr '\n' ':')" \
    com.golddoc.websocket.WebSocketTestClient test-user-1
```

### Features
1. **Room-based messaging** - Send messages to specific rooms
2. **Broadcast messaging** - Send messages to all connected users
3. **User management** - Track connected users and their rooms
4. **Automatic reconnection** - Handles connection drops gracefully
5. **JSON message format** - Structured message format with type safety
6. **Connection status API** - REST API to monitor WebSocket server status

### Monitoring
- Check WebSocket server status: `GET /api/websocket/status`
- Health check: `GET /api/websocket/health`
- Connection info: `GET /api/websocket/info`

## Support

For issues and feature requests, please create an issue in the GitHub repository.