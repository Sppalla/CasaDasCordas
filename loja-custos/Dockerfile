# Build
FROM maven:3.9-eclipse-temurin-17-alpine AS build
WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN mvn -q -DskipTests package

# Run (Render injeta a variável PORT)
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
COPY --from=build /app/target/loja-custos-*.jar /app/app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
