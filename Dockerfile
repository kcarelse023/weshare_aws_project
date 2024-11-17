# Step 1: Use a Maven image to build the project
FROM maven:3.9.4-eclipse-temurin-17 AS build

# Set the working directory inside the container
WORKDIR /app

# Copy the entire project into the container
COPY . .

# Compile the application (skip tests if necessary)
RUN mvn clean package -DskipTests
RUN ls -a

# Expose the port the application will run on
EXPOSE 5050

# Step 2: Don't run the application explicitly here
# Comment out or remove the CMD line
CMD [ "java", "-jar", "target/weshare-mvc-exercise-1.0-SNAPSHOT-jar-with-dependencies.jar" ]
