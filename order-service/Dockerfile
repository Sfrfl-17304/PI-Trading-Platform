FROM eclipse-temurin:21-jdk AS build
WORKDIR /app
COPY .mvn/ .mvn/
COPY mvnw pom.xml ./
COPY src/ src/
RUN chmod +x mvnw && ./mvnw -q -Dmaven.test.skip=true package

FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
ENV JAVA_OPTS=""
COPY --from=build /app/target/order-service-*.jar /app/order-service.jar
EXPOSE 8082
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar /app/order-service.jar"]
