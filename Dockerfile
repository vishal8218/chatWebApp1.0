# ----------------------------
# Build Stage
# ----------------------------
FROM maven:3.9.6-eclipse-temurin-11 AS build

# Set working directory
WORKDIR /chatApp

# Copy pom.xml and download dependencies
COPY pom.xml .

RUN mvn dependency:go-offline

# Copy source code
COPY src ./src

# Build the application
RUN mvn clean package -DskipTests

# Verify the generated JAR
RUN ls -la target/

# ----------------------------
# Runtime Stage
# ----------------------------
FROM eclipse-temurin:11-jre

# Set working directory
WORKDIR /app

# Copy JAR from build stage
COPY --from=build /chatApp/target/Message_Sharing-0.0.1-SNAPSHOT.jar app.jar

# Expose application port
EXPOSE 8080

# Run the application
ENTRYPOINT ["java", "-jar", "app.jar"]
