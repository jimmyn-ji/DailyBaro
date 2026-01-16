FROM openjdk:17-jdk-slim
WORKDIR /app
COPY target/ai-knowledge-service-1.0-SNAPSHOT.jar app.jar
EXPOSE 8013
ENTRYPOINT ["java", "-jar", "app.jar"]
