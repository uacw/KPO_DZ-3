FROM gradle:8.5-jdk17 AS builder
WORKDIR /app
# Copy build config for caching
COPY settings.gradle .
COPY build.gradle .
COPY api-gateway/build.gradle api-gateway/

# Download dependencies (will be cached)
RUN gradle :api-gateway:dependencies --no-daemon

# Copy source code
COPY api-gateway/src api-gateway/src

# Build
RUN gradle :api-gateway:build -x test --no-daemon

FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
COPY --from=builder /app/api-gateway/build/libs/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
