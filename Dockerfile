#FROM eclipse-temurin:21-jre-alpine
#WORKDIR /app
#
#ARG JAR_FILE=target/*.jar
#
#COPY ${JAR_FILE} /app/app.jar
#
#EXPOSE 8080
#
#ENTRYPOINT ["java", "-jar", "app.jar"]

# Solução para Máquina SENAI instável
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN mvn clean package -DskipTests

FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY --from=build /app/target/blink-link-*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]