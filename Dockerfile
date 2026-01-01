# build (with tests) using Gradle Wrapper
FROM eclipse-temurin:21-jdk AS builder
WORKDIR /workspace
COPY . .
RUN chmod +x gradlew && ./gradlew clean test :remittance-api:bootJar --no-daemon

# runtime
FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=builder /workspace/remittance-api/build/libs/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java","-jar","/app/app.jar"]
