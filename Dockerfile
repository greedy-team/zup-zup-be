FROM amazoncorretto:21-alpine-jdk

WORKDIR /app

COPY build/libs/sejong-zupzup-0.0.1-SNAPSHOT.jar /app.jar

ENTRYPOINT ["java", "-Duser.timezone=Asia/Seoul", "-jar", "/app.jar"]

CMD ["-Dspring.profiles.active=prod"]

EXPOSE 8080
EXPOSE 8081
