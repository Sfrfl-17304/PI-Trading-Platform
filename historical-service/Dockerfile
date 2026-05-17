FROM eclipse-temurin:21-jdk AS build
WORKDIR /app
COPY .mvn/ .mvn/
COPY mvnw pom.xml ./
COPY src/ src/
RUN chmod +x mvnw && ./mvnw -q -DskipTests package

FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
ENV JAVA_OPTS=""
COPY --from=build /app/target/historical-service-*.jar /app/historical-service.jar
EXPOSE 8087
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar /app/historical-service.jar"]
