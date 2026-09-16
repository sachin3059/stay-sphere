FROM eclipse-temurin:21-jdk-alpine AS builder
WORKDIR /workspace
COPY . .
RUN chmod +x gradlew && ./gradlew :notification-service:bootJar -x test --no-daemon

FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY --from=builder /workspace/notification-service/build/libs/*.jar app.jar
ENV JAVA_OPTS="-Duser.timezone=UTC"
EXPOSE 8087
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
