package com.greedy.zupzup.global.config;

import com.greedy.zupzup.auth.exception.AuthException;
import com.greedy.zupzup.global.exception.InfrastructureException;
import okhttp3.CipherSuite;
import okhttp3.ConnectionSpec;
import okhttp3.JavaNetCookieJar;
import okhttp3.OkHttpClient;
import okhttp3.TlsVersion;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.net.ssl.*;
import java.net.CookieManager;
import java.net.CookiePolicy;
import java.util.Arrays;

@Configuration
public class HttpClientConfig {

    /**
     * 세종대학교 포털 로그인을 요청을 위한 OkHttpClient 객체를 생성합니다.
     */
    @Bean
    public OkHttpClient buildClient() {
        try {
            // SSLContext 생성, 모든 인증서 신뢰 설정
            SSLContext sslCtx = SSLContext.getInstance("SSL");
            sslCtx.init(null, new TrustManager[]{trustAllManager()}, new java.security.SecureRandom());
            SSLSocketFactory sslFactory = sslCtx.getSocketFactory();

            // 세종대 서버의 AES256-SHA(RSA_WITH_AES_256_CBC_SHA) 허용 설정 추가
            ConnectionSpec spec = new ConnectionSpec.Builder(ConnectionSpec.MODERN_TLS)
                    .tlsVersions(TlsVersion.TLS_1_2, TlsVersion.TLS_1_1, TlsVersion.TLS_1_0)
                    .cipherSuites(
                            CipherSuite.TLS_ECDHE_ECDSA_WITH_AES_128_GCM_SHA256,
                            CipherSuite.TLS_ECDHE_RSA_WITH_AES_128_GCM_SHA256,
                            CipherSuite.TLS_RSA_WITH_AES_256_CBC_SHA,
                            CipherSuite.TLS_RSA_WITH_AES_128_CBC_SHA
                    )
                    .build();

            HostnameVerifier hostnameVerifier = (hostname, session) -> true;

            // 쿠키 관리
            CookieManager cookieManager = new CookieManager();
            cookieManager.setCookiePolicy(CookiePolicy.ACCEPT_ALL);

            // OkHttpClient 생성
            return new OkHttpClient.Builder()
                    .sslSocketFactory(sslFactory, trustAllManager())
                    .hostnameVerifier(hostnameVerifier)
                    .connectionSpecs(Arrays.asList(spec, ConnectionSpec.CLEARTEXT))
                    .cookieJar(new JavaNetCookieJar(cookieManager))
                    .build();

        } catch (Exception e) {
            // 상세 원인 파악을 위해 스택트레이스 출력 추가
            e.printStackTrace();
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