FROM maven:3.8.6-eclipse-temurin-11 AS build
COPY src /usr/src/app/src
COPY pom.xml /usr/src/app
COPY .openapi-generator-ignore /usr/src/app
RUN mvn -f /usr/src/app/pom.xml clean package

FROM eclipse-temurin:11-jre-jammy
COPY --from=build /usr/src/app/target/psc-api-maj-*.jar /usr/app/psc-api-maj.jar
USER daemon
EXPOSE 8080
ENTRYPOINT ["java","-jar","/usr/app/psc-api-maj.jar"]
