FROM amazoncorretto:21-alpine-jdk

WORKDIR /app

COPY build/libs/sejong-zupzup-0.0.1-SNAPSHOT.jar /app.jar

# 커스텀 보안 설정: RSA CBC 암호화 허용
RUN echo "jdk.tls.disabledAlgorithms=SSLv3, TLSv1, TLSv1.1, RC4, DES, MD5withRSA, \\" > /app/custom.security && \
    echo " DH keySize < 1024, EC keySize < 224, 3DES_EDE_CBC, anon, NULL" >> /app/custom.security && \
    echo "jdk.tls.legacyAlgorithms=" >> /app/custom.security

ENTRYPOINT ["java", \
            "-Duser.timezone=Asia/Seoul", \
            "-Djava.security.properties=/app/custom.security", \
            "-Dhttps.protocols=TLSv1.2", \
            "-Djdk.tls.client.protocols=TLSv1.2", \
            "-jar", "/app.jar"]

CMD ["--spring.profiles.active=prod"]

EXPOSE 8080
EXPOSE 8081