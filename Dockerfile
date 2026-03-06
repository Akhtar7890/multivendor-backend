# Stage 1: Build
FROM eclipse-temurin:17-jdk AS build
WORKDIR /app

# 1. Copy only the Maven wrapper and pom.xml first
COPY .mvn/ .mvn
COPY mvnw pom.xml ./
RUN chmod +x mvnw

# 2. Download dependencies - this layer is CACHED unless pom.xml changes
RUN ./mvnw dependency:go-offline

# 3. Copy source and build the application
COPY src ./src
RUN ./mvnw clean package -DskipTests

# Stage 2: Runtime (keeps the image small)
FROM eclipse-temurin:17-jre
WORKDIR /app

# Copy only the built jar from the build stage
# Replace 'app-name' with your actual artifact ID from pom.xml
COPY --from=build /app/target/*.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]