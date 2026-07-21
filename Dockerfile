# Use OpenJDK 11 as the base image for building the project
FROM openjdk:11 AS build

# Set the working directory inside the container
WORKDIR /chatApp

# Copy the pom.xml first to leverage Docker cache for dependencies
COPY pom.xml .

# Download the dependencies (to optimize build by caching them)
RUN mvn dependency:go-offline

# Copy the entire source code into the container
COPY src ./src

# Package the Spring Boot application (skip tests for faster builds)
RUN mvn clean package -DskipTests

# Use a minimal OpenJDK image to run the app (JRE only)
FROM openjdk:11-jre-slim

# Set the working directory for the runtime container
WORKDIR /chatApp

# Copy the built JAR file from the build stage to the runtime image
COPY --from=build /chatApp/target/Message_Sharing-0.0.1-SNAPSHOT.jar /app/app.jar

# Expose the default port for Spring Boot applications
EXPOSE 8080

# Run the Spring Boot app
ENTRYPOINT ["java", "-jar", "/app/app.jar"]






# Use OpenJDK 11 as the base image for building the project
FROM openjdk:11 AS build

# Set the working directory inside the container
WORKDIR /chatApp

# Copy the pom.xml first to leverage Docker cache for dependencies
COPY pom.xml .

# Download the dependencies (to optimize build by caching them)
RUN mvn dependency:go-offline

# Copy the entire source code into the container
COPY src ./src

# Package the Spring Boot application (skip tests for faster builds)
RUN mvn clean package -DskipTests

# Check if the target directory exists after the build
RUN ls -al /chatApp/target

# Use a minimal OpenJDK image to run the app (JRE only)
FROM openjdk:11-jre-slim

# Set the working directory for the runtime container
WORKDIR /chatApp

# Copy the built JAR file from the build stage to the runtime image
COPY --from=build /demo/target/Message_Sharing-0.0.1-SNAPSHOT.jar /app/app.jar

# Expose the default port for Spring Boot applications
EXPOSE 8080

# Run the Spring Boot app
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
