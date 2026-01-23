package com.greedy.zupzup.global.config;

import com.greedy.zupzup.auth.exception.AuthException;
import com.greedy.zupzup.global.exception.InfrastructureException;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.net.ssl.*;
import java.net.CookieManager;
import java.net.CookiePolicy;
import java.util.Arrays;
import java.util.concurrent.TimeUnit;

@Slf4j
@Configuration
public class HttpClientConfig {

    @Bean
    public OkHttpClient buildClient() {
        try {
            SSLContext sslCtx = SSLContext.getInstance("TLS");
            sslCtx.init(null, new TrustManager[]{trustAllManager()}, new java.security.SecureRandom());

            // [핵심 수정] 세종대 서버와 호환성을 위해 암호화 방식 범위를 넓힙니다.
            ConnectionSpec spec = new ConnectionSpec.Builder(ConnectionSpec.MODERN_TLS)
                    .tlsVersions(TlsVersion.TLS_1_2) // 세종대는 TLS 1.2를 사용함
                    .cipherSuites(
                            // 최신 방식들
                            CipherSuite.TLS_ECDHE_RSA_WITH_AES_128_GCM_SHA256,
                            CipherSuite.TLS_ECDHE_RSA_WITH_AES_256_GCM_SHA384,
                            // [필수] 세종대 서버가 사용하는 이전 방식 (로그의 handshake_failure 해결)
                            CipherSuite.TLS_RSA_WITH_AES_256_CBC_SHA,
                            CipherSuite.TLS_RSA_WITH_AES_128_CBC_SHA,
                            CipherSuite.TLS_RSA_WITH_AES_128_GCM_SHA256
                    )
                    .build();

            CookieManager cookieManager = new CookieManager();
            cookieManager.setCookiePolicy(CookiePolicy.ACCEPT_ALL);

            return new OkHttpClient.Builder()
                    .sslSocketFactory(sslCtx.getSocketFactory(), trustAllManager())
                    .hostnameVerifier((hostname, session) -> true)
                    .connectionSpecs(Arrays.asList(spec, ConnectionSpec.COMPATIBLE_TLS, ConnectionSpec.CLEARTEXT)) // COMPATIBLE_TLS 추가
                    .cookieJar(new JavaNetCookieJar(cookieManager))
                    .connectTimeout(15, TimeUnit.SECONDS)
                    .readTimeout(15, TimeUnit.SECONDS)
                    .build();

        } catch (Exception e) {
            log.error("OkHttpClient Bean 생성 실패: {}", e.getMessage());
            throw new InfrastructureException(AuthException.SEJONG_PORTAL_LOGIN_FAILED);
        }
    }

    private X509TrustManager trustAllManager() {
        return new X509TrustManager() {
            @Override
            public void checkClientTrusted(java.security.cert.X509Certificate[] chain, String authType) {}

            @Override
            public void checkServerTrusted(java.security.cert.X509Certificate[] chain, String authType) {}

            @Override
            public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                return new java.security.cert.X509Certificate[0];
            }
        };
    }
}