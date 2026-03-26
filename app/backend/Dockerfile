FROM gradle:8.10.2-jdk21 AS builder
WORKDIR /workspace

# Copy monorepo build metadata first to maximize Docker layer cache hits.
COPY build.gradle.kts settings.gradle.kts gradlew gradlew.bat /workspace/
COPY gradle /workspace/gradle
COPY app/backend/build.gradle.kts app/backend/gradle.properties /workspace/app/backend/

# Warm up Gradle dependency cache for the backend module.
RUN --mount=type=cache,target=/home/gradle/.gradle \
    gradle --no-daemon :backend:dependencies

# Copy backend sources.
COPY app/backend/src /workspace/app/backend/src

RUN --mount=type=cache,target=/home/gradle/.gradle \
    gradle --no-daemon :backend:bootJar

FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY --from=builder /workspace/app/backend/build/libs/*.jar /app/app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
