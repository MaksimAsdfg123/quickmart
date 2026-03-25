FROM gradle:8.10.2-jdk21 AS builder
WORKDIR /app

# Copy build metadata first to maximize Docker layer cache hits.
COPY build.gradle.kts settings.gradle.kts gradle.properties /app/

# Warm up Gradle dependency cache.
RUN --mount=type=cache,target=/home/gradle/.gradle \
    gradle --no-daemon dependencies

COPY src /app/src

RUN --mount=type=cache,target=/home/gradle/.gradle \
    gradle --no-daemon bootJar -x test

FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY --from=builder /app/build/libs/*.jar /app/app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
