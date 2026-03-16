FROM eclipse-temurin:21-jdk-jammy AS build
WORKDIR /app

COPY gradlew build.gradle settings.gradle ./
COPY gradle gradle
RUN chmod +x gradlew

RUN --mount=type=cache,target=/root/.gradle ./gradlew dependencies --no-daemon

COPY src src
RUN --mount=type=cache,target=/root/.gradle ./gradlew bootJar --no-daemon

FROM eclipse-temurin:21-jre-jammy
WORKDIR /app

RUN useradd -ms /bin/bash appuser

COPY --from=build --chown=appuser:appuser /app/build/libs/*.jar app.jar

USER appuser

ENV JAVA_OPTS="-Xms256m -Xmx512m"

EXPOSE 8085

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]