# Build JARs on the host first:  ./gradlew bootJar -x test
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY property-service/build/libs/*.jar app.jar
ENV JAVA_OPTS="-Duser.timezone=UTC"
EXPOSE 8082
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
