FROM gradle:8.5-jdk17 AS builder
WORKDIR /app
# Copy build config
COPY settings.gradle .
COPY build.gradle .
COPY file-analysis-service/build.gradle file-analysis-service/
RUN gradle :file-analysis-service:dependencies --no-daemon

# Copy source
COPY file-analysis-service/src file-analysis-service/src

# Build
RUN gradle :file-analysis-service:build -x test --no-daemon

FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
RUN mkdir -p /app/data
COPY --from=builder /app/file-analysis-service/build/libs/*.jar app.jar
EXPOSE 8082
ENTRYPOINT ["java", "-jar", "app.jar"]
