FROM openjdk:17-jdk-slim

WORKDIR /app

COPY target/rubric-service-0.0.1-SNAPSHOT.jar /app/rubric.jar

EXPOSE 8084

CMD ["java", "-jar", "rubric.jar"]

