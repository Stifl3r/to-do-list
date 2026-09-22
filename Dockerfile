FROM eclipse-temurin:25-jre-alpine
WORKDIR /app

# Target Gradle's output folder instead of Maven's target/ folder
COPY build/libs/*-SNAPSHOT.jar app.jar

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
