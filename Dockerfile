# Use a slim JDK base image
FROM eclipse-temurin:17-jdk-alpine as builder

WORKDIR /app

# Copy and build
COPY . .
RUN ./mvn clean package -DskipTests

# Final image for running the app
FROM eclipse-temurin:17-jdk-alpine
VOLUME /tmp
COPY --from=builder /app/target/*.jar app.jar

ENTRYPOINT ["java", "-jar", "/app.jar"]
