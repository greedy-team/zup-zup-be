package com.greedy.zupzup.auth.application;

import com.greedy.zupzup.auth.application.dto.SejongAuthInfo;
import com.greedy.zupzup.auth.exception.AuthException;
import com.greedy.zupzup.global.exception.ApplicationException;
import com.greedy.zupzup.global.exception.InfrastructureException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.stereotype.Component;

import javax.net.ssl.*;
import java.io.IOException;
import java.net.CookieManager;
import java.net.CookiePolicy;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class SejongAuthenticator {

    private static final int MAX_PORTAL_LOGIN_RETRY_COUNT = 3;
    private static final int STUDENT_INFO_MAJOR_INDEX = 0;
    private static final int STUDENT_INFO_ID_INDEX = 1;
    private static final int STUDENT_INFO_NAME_INDEX = 2;

    private static final String SEJONG_PORTAL_LOGIN_URL = "https://portal.sejong.ac.kr/jsp/login/login_action.jsp";
    private static final String SEJONG_SSO_URL = "http://classic.sejong.ac.kr/_custom/sejong/sso/sso-return.jsp?returnUrl=https://classic.sejong.ac.kr/classic/index.do";
    private static final String SEJONG_READING_SITE_URL = "https://classic.sejong.ac.kr/classic/reading/status.do";
    private static final String STUDENT_INFO_TABLE_TR = ".b-con-box:has(h4.b-h4-tit01:contains(사용자 정보)) table.b-board-table tbody tr";
    private static final String SEJONG_PORTAL_LOGIN_SUCCESS_MESSAGE_IN_HTML = "var result = 'OK'";


    /**
     * 세종대학교 포털 로그인을 통해 학생 인증을 진행합니다.
     */
    public SejongAuthInfo getStudentAuthInfo(String portalId, String portalPassword) {

        try {
            OkHttpClient client = buildClient();
            doPortalLogin(client, portalId, portalPassword);

            ssoRedirectToReadingSite(client);
            String readingPageHtml = fetchReadingPageHtml(client);

            return parseHTMLAndGetMemberInfo(readingPageHtml);
        } catch (IOException e) {
            throw new InfrastructureException(AuthException.SEJONG_PORTAL_LOGIN_FAILED);
        }
    }


    /**
     * 세종대학교 포털 로그인을 진행합니다.
     */
    private void doPortalLogin(OkHttpClient client, String portalId, String portalPassword) throws IOException {

        FormBody formBody = new FormBody.Builder()
                .add("mainLogin", "N")
                .add("rtUrl", "library.sejong.ac.kr")
                .add("id", portalId)
                .add("password", portalPassword)
                .build();

        Request request = new Request.Builder()
                .url(SEJONG_PORTAL_LOGIN_URL)
                .post(formBody)
                .header("Host", "portal.sejong.ac.kr")
                .header("Referer", "https://portal.sejong.ac.kr")
                .header("Cookie", "chknos=false")
                .build();
        
        Response response = executeWithRetry(client, request);

        String body = response.body() != null ? response.body().string() : "";
        response.close();

        System.out.println(body);

        // var result = 'OK' 라는 코드가 있으면 로그인 성공 -> 그외 로그인 실패
        if (!body.contains(SEJONG_PORTAL_LOGIN_SUCCESS_MESSAGE_IN_HTML)) {
            throw new ApplicationException(AuthException.INVALID_SEJONG_PORTAL_LOGIN_ID_PW);
        }

    }


    /**
     * 세종대학교 포털 로그인을 요청을 위한 OkHttpClient 객체를 생성합니다.
     */
    private OkHttpClient buildClient() {
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


    /**
     * 포털 로그인 성공 후 생성된 세션(쿠키)을 이용하여 고전독서인증 사이트로 SSO 인증을 요청합니다
     */
    private void ssoRedirectToReadingSite(OkHttpClient client) throws IOException {
        Request ssoReq = new Request.Builder().url(SEJONG_SSO_URL).get().build();
        try (Response ssoResp = client.newCall(ssoReq).execute()) {
            if (!ssoResp.isSuccessful()) {
                throw new InfrastructureException(AuthException.SEJONG_PORTAL_LOGIN_FAILED);
            }
        }
    }


    /**
     * 로그인된 세션을 사용하여 고전독서인증 페이지의 HTML을 가져옵니다.
     */
    private String fetchReadingPageHtml(OkHttpClient client) throws IOException {

        Request readingSiteRequest = new Request.Builder()
                .url(SEJONG_READING_SITE_URL)
                .get()
                .build();

        try (Response finalResp = client.newCall(readingSiteRequest).execute()) {
            if (finalResp.body() == null || finalResp.code() != 200) {
                throw new InfrastructureException(AuthException.SEJONG_PORTAL_LOGIN_FAILED);
            }
            return finalResp.body().string();
        }
    }


    /**
     * 고전독서 페이지에서 학생 정보를 추출합니다.
     */
    private SejongAuthInfo parseHTMLAndGetMemberInfo(String html) {
        Document doc = Jsoup.parse(html);

        List<String> rowValues = new ArrayList<>();

        doc.select(STUDENT_INFO_TABLE_TR).forEach(tr -> {
            String value = tr.select("td").text().trim();
            rowValues.add(value);
        });

        String major = getValueFromList(rowValues, STUDENT_INFO_MAJOR_INDEX);   // 일단 사용 x
        String studentId = getValueFromList(rowValues, STUDENT_INFO_ID_INDEX);
        String studentName = getValueFromList(rowValues, STUDENT_INFO_NAME_INDEX);

        return new SejongAuthInfo(studentId, studentName);
    }

    private String getValueFromList(List<String> list, int index) {
        return list.size() > index ? list.get(index) : null;
    }


    /**
     * 포털 로그인을 시도합니다.
     */
    private Response executeWithRetry(OkHttpClient client, Request request) throws IOException {
        Response response = null;
        int tryCount = 0;
        while (tryCount < MAX_PORTAL_LOGIN_RETRY_COUNT) {
            try {
                response = client.newCall(request).execute();
                if (response.isSuccessful()) {
                    return response;
                }
            } catch (SocketTimeoutException e) {
                tryCount++;
                log.warn("포탈 로그인 타임아웃 발생 (재시도: {}회)", tryCount);
            }
        }
        throw new InfrastructureException(AuthException.SEJONG_PORTAL_LOGIN_FAILED);
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
