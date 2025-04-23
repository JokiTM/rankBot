FROM maven:3.9.8-eclipse-temurin-21 AS build
WORKDIR /app
RUN java -version
COPY pom.xml .
COPY src ./src
RUN mvn clean package -DskipTests
RUN ls -l target/*.jar

# Runtime stage
FROM eclipse-temurin:21-jre
RUN java -version
WORKDIR /app
COPY --from=build /app/target/*.jar bot.jar
# Add verbose logging
ENTRYPOINT ["java", "-verbose:class", "-jar", "bot.jar"]
