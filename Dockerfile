FROM amazoncorretto:21-alpine-jdk

WORKDIR /app

COPY build/libs/sejong-zupzup-0.0.1-SNAPSHOT.jar /app.jar

ENTRYPOINT ["java", "-jar", "/app.jar"]

EXPOSE 8080
