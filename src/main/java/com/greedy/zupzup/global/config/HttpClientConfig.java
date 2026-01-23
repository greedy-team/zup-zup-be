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
import java.security.KeyStore;
import java.util.Arrays;
import java.util.concurrent.TimeUnit;

@Slf4j
@Configuration
public class HttpClientConfig {

    @Bean
    public OkHttpClient buildClient() {
        try {
            X509TrustManager trustManager = getDefaultTrustManager();

            SSLContext sslCtx = SSLContext.getInstance("TLSv1.2");
            sslCtx.init(null, new TrustManager[]{trustManager}, new java.security.SecureRandom());

            ConnectionSpec sejongSpec = new ConnectionSpec.Builder(ConnectionSpec.COMPATIBLE_TLS)
                    .tlsVersions(TlsVersion.TLS_1_2)
                    .cipherSuites(
                            CipherSuite.TLS_RSA_WITH_AES_256_CBC_SHA,
                            CipherSuite.TLS_RSA_WITH_AES_128_CBC_SHA,
                            CipherSuite.TLS_ECDHE_RSA_WITH_AES_256_GCM_SHA384,
                            CipherSuite.TLS_ECDHE_RSA_WITH_AES_128_GCM_SHA256,
                            CipherSuite.TLS_RSA_WITH_AES_256_GCM_SHA384,
                            CipherSuite.TLS_RSA_WITH_AES_128_GCM_SHA256
                    )
                    .build();

            CookieManager cookieManager = new CookieManager();
            cookieManager.setCookiePolicy(CookiePolicy.ACCEPT_ALL);

            return new OkHttpClient.Builder()
                    .sslSocketFactory(sslCtx.getSocketFactory(), trustManager)
                    .connectionSpecs(Arrays.asList(sejongSpec, ConnectionSpec.CLEARTEXT))
                    .cookieJar(new JavaNetCookieJar(cookieManager))
                    .connectTimeout(15, TimeUnit.SECONDS)
                    .readTimeout(15, TimeUnit.SECONDS)
                    .writeTimeout(15, TimeUnit.SECONDS)
                    .build();

        } catch (Exception e) {
            log.error("OkHttpClient Bean 생성 실패: {}", e.getMessage());
            throw new InfrastructureException(AuthException.SEJONG_PORTAL_LOGIN_FAILED);
        }
    }

    /**
     * 시스템 기본 TrustManager 가져오기 (인증서 검증 유지)
     */
    private X509TrustManager getDefaultTrustManager() throws Exception {
        TrustManagerFactory factory = TrustManagerFactory.getInstance(
                TrustManagerFactory.getDefaultAlgorithm()
        );
        factory.init((KeyStore) null);

        for (TrustManager tm : factory.getTrustManagers()) {
            if (tm instanceof X509TrustManager) {
                return (X509TrustManager) tm;
            }
        }
        throw new IllegalStateException("X509TrustManager를 찾을 수 없습니다");
    }
}