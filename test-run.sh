#!/bin/bash

echo "=== Gold Document Management System Test Script ==="
echo ""

# Check Java version
echo "1. Checking Java version..."
java -version
echo ""

# Check Maven version
echo "2. Checking Maven version..."
mvn --version
echo ""

# Clean and compile
echo "3. Cleaning and compiling project..."
mvn clean compile
if [ $? -eq 0 ]; then
    echo "✓ Compilation successful"
else
    echo "✗ Compilation failed"
    exit 1
fi
echo ""

# Run tests
echo "4. Running tests..."
mvn test
if [ $? -eq 0 ]; then
    echo "✓ Tests passed"
else
    echo "✗ Tests failed"
    exit 1
fi
echo ""

# Check test coverage
echo "5. Generating test coverage report..."
mvn jacoco:report
if [ $? -eq 0 ]; then
    echo "✓ Coverage report generated at target/site/jacoco/index.html"
else
    echo "✗ Coverage report generation failed"
fi
echo ""

# Package the application
echo "6. Packaging application..."
mvn package -DskipTests
if [ $? -eq 0 ]; then
    echo "✓ Package created: target/gold-doc-1.0.0.jar"
    echo "  Size: $(du -h target/gold-doc-1.0.0.jar | cut -f1)"
else
    echo "✗ Packaging failed"
    exit 1
fi
echo ""

# Check project structure
echo "7. Project structure check..."
echo "Total Java files: $(find src -name "*.java" | wc -l)"
echo "Main source files: $(find src/main -name "*.java" | wc -l)"
echo "Test source files: $(find src/test -name "*.java" | wc -l)"
echo ""

# Display API endpoints
echo "8. Available API endpoints:"
echo "   GET    /api/documents           - Get all documents"
echo "   GET    /api/documents/{id}      - Get document by ID"
echo "   POST   /api/documents           - Create a new document"
echo "   PUT    /api/documents/{id}      - Update a document"
echo "   DELETE /api/documents/{id}      - Delete a document"
echo "   GET    /api/documents/status/*  - Get documents by status"
echo "   GET    /api/documents/search    - Search documents"
echo "   GET    /api/documents/type/*    - Get documents by type"
echo "   GET    /api/documents/stats     - Get document statistics"
echo "   GET    /api/documents/health    - Health check"
echo ""

# Display access URLs
echo "9. Application URLs (when running):"
echo "   Application:      http://localhost:8080/api"
echo "   Swagger UI:       http://localhost:8080/api/swagger-ui.html"
echo "   API Docs:         http://localhost:8080/api/api-docs"
echo "   H2 Console:       http://localhost:8080/api/h2-console"
echo "   Default credentials: admin / admin123"
echo ""

echo "=== Test completed successfully ==="
echo "To run the application: mvn spring-boot:run"