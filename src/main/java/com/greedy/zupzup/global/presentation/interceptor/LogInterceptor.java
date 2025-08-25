package com.greedy.zupzup.global.presentation.interceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;


@Slf4j
@Component
public class LogInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String requestId = MDC.get("request_id");
        String requestURI = request.getRequestURI();
        String httpMethod = request.getMethod();
        String clientIp = request.getRemoteAddr();

        log.info("[{}] REQUEST | {} {} | IP: {}", requestId, httpMethod, requestURI, clientIp);
        request.setAttribute("startTime", System.currentTimeMillis());
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        String requestId = MDC.get("request_id");
        String requestURI = request.getRequestURI();
        String httpMethod = request.getMethod();
        long startTime = (Long) request.getAttribute("startTime");
        long executionTime = System.currentTimeMillis() - startTime;

        log.info("[{}] RESPONSE | {} {} | STATUS: {} | Execution Time: {}ms",
                requestId, httpMethod, requestURI, response.getStatus(), executionTime);

        if (ex != null) {
            log.error("[{}] EXCEPTION | ", requestId, ex);
        }
    }
}
