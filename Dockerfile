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
EXPOSE 9010
ENTRYPOINT ["sh","-c","java \
            -Dcom.sun.management.jmxremote \
            -Dcom.sun.management.jmxremote.port=9010 \
            -Dcom.sun.management.jmxremote.rmi.port=9011 \
            -Djava.rmi.server.hostname=localhost \
            -Dcom.sun.management.jmxremote.ssl=false \
            -Dcom.sun.management.jmxremote.authenticate=false \
            -jar /app/libs/$(ls /app/libs | grep -v plain | head -n 1)"]
