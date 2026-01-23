package com.greedy.zupzup.global.config;

import com.greedy.zupzup.auth.exception.AuthException;
import com.greedy.zupzup.global.exception.InfrastructureException;
import okhttp3.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import lombok.extern.slf4j.Slf4j;

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
            // 모든 인증서를 신뢰하도록 설정 (Handshake Failure 해결의 핵심)
            SSLContext sslCtx = SSLContext.getInstance("TLS");
            sslCtx.init(null, new TrustManager[]{trustAllManager()}, new java.security.SecureRandom());

            // 세종대 서버의 낡은 암호화 방식(Cipher Suites)을 모두 허용하도록 강제 설정
            ConnectionSpec spec = new ConnectionSpec.Builder(ConnectionSpec.COMPATIBLE_TLS)
                    .tlsVersions(TlsVersion.TLS_1_2, TlsVersion.TLS_1_1, TlsVersion.TLS_1_0)
                    .allEnabledCipherSuites() // 모든 암호화 알고리즘 활성화
                    .build();

            CookieManager cookieManager = new CookieManager();
            cookieManager.setCookiePolicy(CookiePolicy.ACCEPT_ALL);

            return new OkHttpClient.Builder()
                    .sslSocketFactory(sslCtx.getSocketFactory(), trustAllManager())
                    .hostnameVerifier((hostname, session) -> true)
                    // 중요: COMPATIBLE_TLS와 CLEARTEXT를 둘 다 지원하도록 설정
                    .connectionSpecs(Arrays.asList(spec, ConnectionSpec.CLEARTEXT))
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