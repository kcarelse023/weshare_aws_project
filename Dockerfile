# Step 1: Use a Maven image to build the project
FROM maven:3.9.4-eclipse-temurin-17 AS build

# Set the working directory inside the container
WORKDIR /app

# Copy the entire project into the container
COPY . .

# Compile the application (skip tests if necessary)
RUN mvn clean compile -DskipTests
RUN ls -a

# Step 2: Use a lightweight JDK image to run the application
FROM openjdk:17-jdk-slim

# Set the working directory inside the container
WORKDIR /app

# Copy the source files from the build stage
COPY . .

# Expose the port your application runs on
EXPOSE 80

# Combine `javac` and `java` commands
CMD ["sh", "-c", "javac -d out -cp src src/main/java/weshare/server/WeShareServer.java && java -cp out weshare.server.WeShareServer"]
