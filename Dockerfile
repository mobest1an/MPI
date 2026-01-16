# ---- Build stage (Gradle already installed) ----
FROM gradle:8.13-jdk17 AS build
WORKDIR /home/gradle/project

# Copy build scripts first (better caching)
COPY settings.gradle* build.gradle* gradle.properties* ./
COPY gradle ./gradle
COPY gradlew ./

# Copy the actual project sources
COPY src ./src

# Build
RUN gradle --no-daemon clean bootJar -x test

# ---- Runtime stage ----
FROM eclipse-temurin:17-jre-jammy
WORKDIR /app
COPY --from=build /home/gradle/project/build/libs /app/libs
EXPOSE 8080
ENTRYPOINT ["sh","-c","java -jar /app/libs/$(ls /app/libs | grep -v plain | head -n 1)"]
