FROM eclipse-temurin:21-jdk-alpine
LABEL authors="TanThinh"

WORKDIR /app
COPY target/*.jar app.jar

RUN apk add --no-cache curl

ENTRYPOINT ["java", "-jar", "/app/app.jar"]