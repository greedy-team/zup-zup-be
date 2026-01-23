FROM amazoncorretto:21-alpine-jdk

WORKDIR /app

COPY build/libs/sejong-zupzup-0.0.1-SNAPSHOT.jar /app.jar

# Java 보안 정책에서 CBC 계열 암호화를 legacyAlgorithms에서 제거
RUN JAVA_SECURITY=$(find /usr/lib/jvm -name java.security 2>/dev/null | head -1) && \
    if [ -n "$JAVA_SECURITY" ] && [ -f "$JAVA_SECURITY" ]; then \
        echo "보안 정책 수정: CBC 암호화 허용" && \
        cp "$JAVA_SECURITY" "${JAVA_SECURITY}.backup" && \
        sed -i 's/jdk.tls.legacyAlgorithms=.*CBC.*/jdk.tls.legacyAlgorithms=/' "$JAVA_SECURITY"; \
    fi

ENTRYPOINT ["java", \
            "-Duser.timezone=Asia/Seoul", \
            "-Dhttps.protocols=TLSv1.2", \
            "-Djdk.tls.client.protocols=TLSv1.2", \
            "-jar", "/app.jar"]

CMD ["--spring.profiles.active=prod"]

EXPOSE 8080
EXPOSE 8081