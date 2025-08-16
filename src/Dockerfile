# Use official OpenJDK image
FROM openjdk:17-jdk-slim

# Set working directory inside container
WORKDIR /app

# Copy the JAR file from target folder (after mvn package)
COPY target/*.jar app.jar

# Expose port 8080 (Render expects web services on this port)
EXPOSE 8080

# Run the JAR file
ENTRYPOINT ["java", "-jar", "app.jar"]
