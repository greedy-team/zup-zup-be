FROM amazoncorretto:21-alpine-jdk

WORKDIR /app

COPY build/libs/sejong-zupzup-0.0.1-SNAPSHOT.jar /app.jar

# JVM 보안 옵션을 추가하여 레거시 TLS 알고리즘 허용
ENTRYPOINT ["java", \
            "-Duser.timezone=Asia/Seoul", \
            "-Dhttps.protocols=TLSv1.2", \
            "-Djdk.tls.client.protocols=TLSv1.2", \
            "-Djdk.tls.disabledAlgorithms=SSLv3,RC4,DES,MD5withRSA", \
            "-jar", "/app.jar"]

CMD ["--spring.profiles.active=prod"]

EXPOSE 8080
EXPOSE 8081