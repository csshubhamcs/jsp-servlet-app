# ---- Stage 1: build the WAR with Maven ----
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app

# Cache dependencies first (only re-runs when pom.xml changes).
COPY pom.xml .
RUN mvn -q -e -B -DskipTests dependency:go-offline

# Build the WAR.
COPY src ./src
RUN mvn -q -e -B -DskipTests package

# ---- Stage 2: runtime on Tomcat 10.1 / JDK 21 ----
FROM tomcat:10.1-jdk21-temurin

# Drop the default Tomcat apps and deploy ours as ROOT.
RUN rm -rf /usr/local/tomcat/webapps/*
COPY --from=build /app/target/jsp-servlet-app.war /usr/local/tomcat/webapps/ROOT.war

EXPOSE 8080
CMD ["catalina.sh", "run"]
