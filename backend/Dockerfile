FROM eclipse-temurin:21-jdk AS build
WORKDIR /app
COPY backend/. .
RUN chmod +x mvnw
RUN ./mvnw clean package -DskipTests

FROM eclipse-temurin:21-jre
WORKDIR /app

COPY --from=build /app/target/*.jar app.jar

EXPOSE 10000

CMD ["java", "-Dserver.port=10000", "-jar", "app.jar"]
