FROM eclipse-temurin:21-jdk-alpine
WORKDIR /app
COPY ./target/UserService-1.0-SNAPSHOT.jar app.jar
EXPOSE 8081
ENTRYPOINT ["java", "-jar", "app.jar"]