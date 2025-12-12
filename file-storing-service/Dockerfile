FROM gradle:8.5-jdk17 AS builder
WORKDIR /app
# Copy build config
COPY settings.gradle .
COPY build.gradle .
COPY file-storing-service/build.gradle file-storing-service/
RUN gradle :file-storing-service:dependencies --no-daemon

# Copy source
COPY file-storing-service/src file-storing-service/src

# Build
RUN gradle :file-storing-service:build -x test --no-daemon

FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
RUN mkdir -p /app/data
COPY --from=builder /app/file-storing-service/build/libs/*.jar app.jar
EXPOSE 8081
ENTRYPOINT ["java", "-jar", "app.jar"]
