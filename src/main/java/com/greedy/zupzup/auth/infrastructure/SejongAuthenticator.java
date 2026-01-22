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
    private static final String USER_AGENT = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36";

    private final OkHttpClient client; // 생성자 주입


    /**
     * 세종대학교 포털 로그인을 통해 학생 인증을 진행합니다.
     */
    public SejongAuthInfo getStudentAuthInfo(String portalId, String portalPassword) {
        log.info("[포털 인증] ========== 시작 ==========");
        log.info("[포털 인증] portalId={}", portalId);

        try {
            log.info("[STEP 1/4] 포털 로그인 요청 시작 | url={}", SEJONG_PORTAL_LOGIN_URL);
            doPortalLogin(client, portalId, portalPassword);
            log.info("[STEP 1/4] 포털 로그인 성공");

            log.info("[STEP 2/4] SSO 리다이렉트 시작 | url={}", SEJONG_SSO_URL);
            ssoRedirectToReadingSite(client);
            log.info("[STEP 2/4] SSO 리다이렉트 성공");

            log.info("[STEP 3/4] 고전독서 페이지 요청 시작 | url={}", SEJONG_READING_SITE_URL);
            String readingPageHtml = fetchReadingPageHtml(client);
            log.info("[STEP 3/4] 고전독서 페이지 응답 수신 | htmlLength={}", readingPageHtml != null ? readingPageHtml.length() : 0);

            log.info("[STEP 4/4] HTML 파싱 시작");
            SejongAuthInfo result = parseHTMLAndGetMemberInfo(readingPageHtml);
            log.info("[STEP 4/4] HTML 파싱 성공 | studentId={}", result.studentId());
            log.info("[포털 인증] ========== 완료 ==========");

            return result;
        } catch (IOException e) {
            log.error("[포털 인증] IOException 발생 | type={}, message={}", e.getClass().getSimpleName(), e.getMessage());
            log.error("[포털 인증] 스택트레이스:", e);
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
                .header("User-Agent", USER_AGENT)
                .build();

        log.debug("[doPortalLogin] 요청 헤더: Host=portal.sejong.ac.kr, Referer=https://portal.sejong.ac.kr, User-Agent={}", USER_AGENT);

        try (Response response = executeWithRetry(client, request)) {
            log.info("[doPortalLogin] 응답 수신 | code={}, message={}", response.code(), response.message());
            log.debug("[doPortalLogin] 응답 헤더: {}", response.headers());

            String body = response.body() != null ? response.body().string() : "";
            log.debug("[doPortalLogin] 응답 본문 길이={}", body.length());
            log.debug("[doPortalLogin] 응답 본문 (처음 500자): {}", body.substring(0, Math.min(500, body.length())));

            // var result = 'OK' 라는 코드가 있으면 로그인 성공 -> 그외 로그인 실패
            if (!body.contains(SEJONG_PORTAL_LOGIN_SUCCESS_MESSAGE_IN_HTML)) {
                log.warn("[doPortalLogin] 로그인 실패 - 'var result = OK' 없음");
                log.warn("[doPortalLogin] 응답 본문 전체: {}", body);

                // 일정 횟수 이상 틀려 포털 계정 잠긴 상태.
                if (body.contains(SEJONG_PORTAL_LOGIN_LOCKED_MESSAGE_IN_HTML)) {
                    log.warn("[doPortalLogin] 계정 잠김 감지 (pwdNeedChg)");
                    throw new ApplicationException(AuthException.SEJONG_PORTAL_ACCOUNT_LOCKED);
                }

                throw new ApplicationException(AuthException.INVALID_SEJONG_PORTAL_LOGIN_ID_PW);
            }
            log.info("[doPortalLogin] 로그인 성공 - 'var result = OK' 확인됨");
        }
    }


    /**
     * 포털 로그인 성공 후 생성된 세션(쿠키)을 이용하여 고전독서인증 사이트로 SSO 인증을 요청합니다
     */
    private void ssoRedirectToReadingSite(OkHttpClient client) throws IOException {
        Request ssoReq = new Request.Builder()
                .url(SEJONG_SSO_URL)
                .header("User-Agent", USER_AGENT)
                .get()
                .build();

        log.debug("[ssoRedirect] 요청 시작 | url={}", SEJONG_SSO_URL);

        try (Response ssoResp = client.newCall(ssoReq).execute()) {
            log.info("[ssoRedirect] 응답 수신 | code={}, message={}, isSuccessful={}",
                    ssoResp.code(), ssoResp.message(), ssoResp.isSuccessful());
            log.debug("[ssoRedirect] 응답 헤더: {}", ssoResp.headers());

            if (!ssoResp.isSuccessful()) {
                String body = ssoResp.body() != null ? ssoResp.body().string() : "";
                log.error("[ssoRedirect] SSO 실패 | code={}, body={}", ssoResp.code(), body);
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
                .header("User-Agent", USER_AGENT)
                .get()
                .build();

        log.debug("[fetchReadingPage] 요청 시작 | url={}", SEJONG_READING_SITE_URL);

        try (Response finalResp = client.newCall(readingSiteRequest).execute()) {
            log.info("[fetchReadingPage] 응답 수신 | code={}, message={}, hasBody={}",
                    finalResp.code(), finalResp.message(), finalResp.body() != null);
            log.debug("[fetchReadingPage] 응답 헤더: {}", finalResp.headers());

            if (finalResp.body() == null || finalResp.code() != 200) {
                log.error("[fetchReadingPage] 실패 | code={}, hasBody={}", finalResp.code(), finalResp.body() != null);
                throw new InfrastructureException(AuthException.SEJONG_PORTAL_LOGIN_FAILED);
            }

            String html = finalResp.body().string();
            log.debug("[fetchReadingPage] HTML 수신 | length={}", html.length());
            log.trace("[fetchReadingPage] HTML 내용 (처음 1000자): {}", html.substring(0, Math.min(1000, html.length())));
            return html;
        }
    }


    /**
     * 고전독서 페이지에서 학생 정보를 추출합니다.
     */
    private SejongAuthInfo parseHTMLAndGetMemberInfo(String html) {
        log.debug("[parseHTML] 파싱 시작 | htmlLength={}", html != null ? html.length() : 0);

        Document doc = Jsoup.parse(html);

        List<String> rowValues = new ArrayList<>();

        doc.select(STUDENT_INFO_TABLE_TR).forEach(tr -> {
            String value = tr.select("td").text().trim();
            rowValues.add(value);
        });

        log.debug("[parseHTML] 추출된 행 개수={}, 값들={}", rowValues.size(), rowValues);

        String major = getValueFromList(rowValues, STUDENT_INFO_MAJOR_INDEX);   // 사용 x
        String studentIdString = getValueFromList(rowValues, STUDENT_INFO_ID_INDEX);
        String studentName = getValueFromList(rowValues, STUDENT_INFO_NAME_INDEX);

        log.debug("[parseHTML] 추출 결과 | major={}, studentId={}, studentName={}", major, studentIdString, studentName);

        if (studentIdString == null || studentIdString.isBlank() || studentName == null || studentName.isBlank()) {
            log.error("[parseHTML] 학생 정보 추출 실패 | studentId={}, studentName={}", studentIdString, studentName);
            log.error("[parseHTML] HTML 전체 내용: {}", html);
            throw new InfrastructureException(AuthException.SEJONG_PORTAL_LOGIN_FAILED);
        }

        try {
            int studentId = Integer.parseInt(studentIdString);
            log.info("[parseHTML] 파싱 성공 | studentId={}", studentId);
            return new SejongAuthInfo(studentId);
        } catch (NumberFormatException e) {
            log.error("[parseHTML] 학번 정수 파싱 오류 | studentIdString={}, studentName={}", studentIdString, studentName);
            throw new InfrastructureException(AuthException.SEJONG_PORTAL_LOGIN_FAILED);
        }
    }

    private String getValueFromList(List<String> list, int index) {
        return list.size() > index ? list.get(index) : null;
    }


    /**
     * 포털 로그인을 시도합니다.
     */
    private Response executeWithRetry(OkHttpClient client, Request request) throws IOException {
        log.debug("[executeWithRetry] 재시도 로직 시작 | maxRetry={}", MAX_PORTAL_LOGIN_RETRY_COUNT);

        Response response;
        int tryCount = 0;
        while (tryCount < MAX_PORTAL_LOGIN_RETRY_COUNT) {
            try {
                log.debug("[executeWithRetry] 시도 #{} | url={}", tryCount + 1, request.url());
                long startTime = System.currentTimeMillis();

                response = client.newCall(request).execute();

                long elapsed = System.currentTimeMillis() - startTime;
                log.info("[executeWithRetry] 응답 수신 | 시도={}, code={}, isSuccessful={}, elapsed={}ms",
                        tryCount + 1, response.code(), response.isSuccessful(), elapsed);

                if (response.isSuccessful()) {
                    return response;
                }

                log.warn("[executeWithRetry] 응답 실패 | code={}, message={}", response.code(), response.message());
                response.close();   // 실패 시 자원 반납
            } catch (SocketTimeoutException e) {
                log.warn("[executeWithRetry] 타임아웃 발생 | 시도={}, message={}", tryCount + 1, e.getMessage());
            } catch (IOException e) {
                log.error("[executeWithRetry] IO 예외 발생 | 시도={}, type={}, message={}",
                        tryCount + 1, e.getClass().getSimpleName(), e.getMessage());
                throw e;
            }
            tryCount++;
        }

        log.error("[executeWithRetry] 최대 재시도 횟수 초과 | maxRetry={}", MAX_PORTAL_LOGIN_RETRY_COUNT);
        throw new InfrastructureException(AuthException.SEJONG_PORTAL_LOGIN_FAILED);
    }
}