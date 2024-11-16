# FROM openjdk:11-jre-slim
# WORKDIR /usr/src/app
# COPY package*.json ./
# RUN npm install
# COPY . .
# EXPOSE 80
# CMD ["java", "server.js"]



# COPY target/your-app.jar /app.jar
# ENTRYPOINT ["java", "-cp", "src.main.java.weshare.server.WeShareServer.main"]


# Step 1: Use a Maven image to build the project
FROM maven:3.9.4-eclipse-temurin-17 AS build

# Set the working directory inside the container
WORKDIR /app

# Copy the entire project into the container
COPY . .

# Package the application (skip tests if necessary)
RUN mvn clean compile -DskipTests
RUN ls -a
# Step 2: Use a lightweight JDK image to run the application
FROM maven:3.9.4-eclipse-temurin-17

# Set the working directory inside the container
WORKDIR /app

COPY . .
RUN ls -a
# Copy the built JAR file from the build stage
COPY --from=build /app/target/weshare-mvc-exercise-1.0-SNAPSHOT.jar /app/robot_worlds.jar

# Expose the port your application runs on
EXPOSE 80


# Run the application
CMD ["java" ,"-cp", "weshare.server.WeShareServer"]
