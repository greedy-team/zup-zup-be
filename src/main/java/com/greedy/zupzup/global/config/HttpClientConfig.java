package com.greedy.zupzup.global.config;

import com.greedy.zupzup.auth.exception.AuthException;
import com.greedy.zupzup.global.exception.InfrastructureException;
import okhttp3.JavaNetCookieJar;
import okhttp3.OkHttpClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.net.ssl.*;
import java.net.CookieManager;
import java.net.CookiePolicy;

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

            // hostnameVerifier: 모든 호스트네임에 대해 OK 처리
            HostnameVerifier hostnameVerifier = (hostname, session) -> true;

            // 쿠키 관리
            CookieManager cookieManager = new CookieManager();
            cookieManager.setCookiePolicy(CookiePolicy.ACCEPT_ALL);

            // OkHttpClient 생성
            return new OkHttpClient.Builder()
                    .sslSocketFactory(sslFactory, trustAllManager())
                    .hostnameVerifier(hostnameVerifier)
                    .cookieJar(new JavaNetCookieJar(cookieManager))
                    .build();

        } catch (Exception e) {
            throw new InfrastructureException(AuthException.SEJONG_PORTAL_LOGIN_FAILED);
        }
    }

    private X509TrustManager trustAllManager() {
        return new X509TrustManager() {
            @Override
            public void checkClientTrusted(java.security.cert.X509Certificate[] chain, String authType) {
            }

            @Override
            public void checkServerTrusted(java.security.cert.X509Certificate[] chain, String authType) {
            }

            @Override
            public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                return new java.security.cert.X509Certificate[0];
            }
        };
    }
}
