FROM amazoncorretto:21-alpine-jdk

WORKDIR /app

COPY build/libs/sejong-zupzup-0.0.1-SNAPSHOT.jar /app.jar

# 핵심: RSA 계열 Cipher Suite 활성화
ENTRYPOINT ["java", \
            "-Duser.timezone=Asia/Seoul", \
            "-Dhttps.protocols=TLSv1.2", \
            "-Djdk.tls.client.protocols=TLSv1.2", \
            "-Djdk.tls.client.cipherSuites=TLS_RSA_WITH_AES_256_CBC_SHA,TLS_RSA_WITH_AES_128_CBC_SHA,TLS_ECDHE_RSA_WITH_AES_256_GCM_SHA384,TLS_ECDHE_RSA_WITH_AES_128_GCM_SHA256", \
            "-jar", "/app.jar"]

CMD ["--spring.profiles.active=prod"]

EXPOSE 8080
EXPOSE 8081