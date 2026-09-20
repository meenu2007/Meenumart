FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN mvn clean package -DskipTests

FROM tomcat:9.0-jdk17-temurin
RUN rm -rf /usr/local/tomcat/webapps/*
COPY --from=build /app/target/meenumart.war /usr/local/tomcat/webapps/ROOT.war
ENV CATALINA_OPTS="--add-opens java.base/java.time=ALL_UNNAMED"
EXPOSE 8080
CMD ["catalina.sh", "run"]
