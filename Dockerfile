FROM openjdk:17
WORKDIR /app
COPY build/libs/gamja-1.0.0.jar app.jar
EXPOSE 58080
ENTRYPOINT ["java", "-jar", "app.jar"]
