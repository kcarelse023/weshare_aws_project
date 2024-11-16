# Step 1: Use a Maven image to build the project
FROM maven:3.9.4-eclipse-temurin-17 AS build

# Set the working directory inside the container
WORKDIR /app

# Copy the entire project into the container
COPY . .

# Compile the application (skip tests if necessary)
RUN mvn clean package -DskipTests
RUN ls -a

# Step 2: Use a lightweight JDK image to run the application
FROM openjdk:17-jdk-slim

# Set the working directory inside the container
WORKDIR /app

# Copy the source files from the build stage
COPY . .
COPY --from=build /app/target/ /app/target/
# Expose the port your application runs on
EXPOSE 80

# Combine `javac` and `java` commands
CMD ["java", "-jar", ".\target\weshare-mvc-exercise-1.0-SNAPSHOT-jar-with-dependencies.jar"]
