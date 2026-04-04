FROM eclipse-temurin:17-jdk
WORKDIR /app
COPY build/libs/coupon-api.jar app.jar
ENTRYPOINT ["java", "-jar", "app.jar"]
