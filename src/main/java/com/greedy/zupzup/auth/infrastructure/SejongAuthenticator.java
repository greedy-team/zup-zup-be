package com.greedy.zupzup.auth.infrastructure;

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

import java.io.IOException;
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
    private static final String SEJONG_PORTAL_LOGIN_LOCKED_MESSAGE_IN_HTML = "var result = 'pwdNeedChg'";

    // 브라우저 위장을 위한 필수 헤더
    private static final String USER_AGENT = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36";
    private static final String REFERER_URL = "https://portal.sejong.ac.kr/jsp/login/login.jsp";

    private final OkHttpClient client;

    public SejongAuthInfo getStudentAuthInfo(String portalId, String portalPassword) {
        try {
            doPortalLogin(client, portalId, portalPassword);
            ssoRedirectToReadingSite(client);
            String readingPageHtml = fetchReadingPageHtml(client);
            return parseHTMLAndGetMemberInfo(readingPageHtml);
        } catch (IOException e) {
            log.error("세종대 인증 과정 중 IO 예외 발생: {}", e.getMessage());
            throw new InfrastructureException(AuthException.SEJONG_PORTAL_LOGIN_FAILED);
        }
    }

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
                .header("Referer", REFERER_URL)
                .header("User-Agent", USER_AGENT)
                .header("Content-Type", "application/x-www-form-urlencoded")
                .header("Cookie", "chknos=false")
                .build();

        try (Response response = executeWithRetry(client, request)) {
            String body = response.body() != null ? response.body().string() : "";

            // 차단 페이지(Alert!!!) 여부 확인
            if (body.contains("Alert!!!") || body.contains("접속을 차단 합니다")) {
                log.error("!!! [차단] 세종대 방화벽이 요청을 거부함. IP 또는 헤더 이슈.");
                throw new InfrastructureException(AuthException.SEJONG_PORTAL_LOGIN_FAILED);
            }

            if (!body.contains(SEJONG_PORTAL_LOGIN_SUCCESS_MESSAGE_IN_HTML)) {
                if (body.contains(SEJONG_PORTAL_LOGIN_LOCKED_MESSAGE_IN_HTML)) {
                    throw new ApplicationException(AuthException.SEJONG_PORTAL_ACCOUNT_LOCKED);
                }
                throw new ApplicationException(AuthException.INVALID_SEJONG_PORTAL_LOGIN_ID_PW);
            }
        }
    }

    private void ssoRedirectToReadingSite(OkHttpClient client) throws IOException {
        Request ssoReq = new Request.Builder()
                .url(SEJONG_SSO_URL)
                .header("User-Agent", USER_AGENT)
                .get()
                .build();
        try (Response ssoResp = client.newCall(ssoReq).execute()) {
            if (!ssoResp.isSuccessful()) {
                throw new InfrastructureException(AuthException.SEJONG_PORTAL_LOGIN_FAILED);
            }
        }
    }

    private String fetchReadingPageHtml(OkHttpClient client) throws IOException {
        Request readingSiteRequest = new Request.Builder()
                .url(SEJONG_READING_SITE_URL)
                .header("User-Agent", USER_AGENT)
                .get()
                .build();

        try (Response finalResp = client.newCall(readingSiteRequest).execute()) {
            if (finalResp.body() == null || finalResp.code() != 200) {
                throw new InfrastructureException(AuthException.SEJONG_PORTAL_LOGIN_FAILED);
            }
            return finalResp.body().string();
        }
    }

    private SejongAuthInfo parseHTMLAndGetMemberInfo(String html) {
        Document doc = Jsoup.parse(html);
        List<String> rowValues = new ArrayList<>();
        doc.select(STUDENT_INFO_TABLE_TR).forEach(tr -> {
            String value = tr.select("td").text().trim();
            rowValues.add(value);
        });

        String studentIdString = getValueFromList(rowValues, STUDENT_INFO_ID_INDEX);
        String studentName = getValueFromList(rowValues, STUDENT_INFO_NAME_INDEX);

        if (studentIdString == null || studentIdString.isBlank()) {
            log.warn("학생 정보 파싱 실패. 응답 HTML 확인 필요.");
            throw new InfrastructureException(AuthException.SEJONG_PORTAL_LOGIN_FAILED);
        }

        try {
            int studentId = Integer.parseInt(studentIdString);
            return new SejongAuthInfo(studentId);
        } catch (NumberFormatException e) {
            throw new InfrastructureException(AuthException.SEJONG_PORTAL_LOGIN_FAILED);
        }
    }

    private String getValueFromList(List<String> list, int index) {
        return list.size() > index ? list.get(index) : null;
    }

    private Response executeWithRetry(OkHttpClient client, Request request) throws IOException {
        int tryCount = 0;
        while (tryCount < MAX_PORTAL_LOGIN_RETRY_COUNT) {
            try {
                Response response = client.newCall(request).execute();
                if (response.isSuccessful()) return response;
                response.close();
            } catch (SocketTimeoutException e) {
                log.warn("타임아웃 발생 (재시도: {}회)", tryCount + 1);
            }
            tryCount++;
        }
        throw new InfrastructureException(AuthException.SEJONG_PORTAL_LOGIN_FAILED);
    }
}