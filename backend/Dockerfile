FROM maven:3.9-eclipse-temurin-11 AS build

WORKDIR /app
COPY pom.xml .
COPY src ./src

RUN mvn clean package -DskipTests

FROM tomcat:9.0-jdk11-temurin

RUN sed -i 's/port="8080"/port="10000"/' /usr/local/tomcat/conf/server.xml

COPY --from=build /app/target/cookwish.war /usr/local/tomcat/webapps/ROOT.war

EXPOSE 10000

CMD ["catalina.sh", "run"]