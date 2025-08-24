package com.greedy.zupzup.global.aop;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.Objects;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.slf4j.MDC;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Slf4j
@Aspect
@Component
@Profile({"dev"})
public class LogAspect {

    @Pointcut("execution(* com.greedy.zupzup..*Controller.*(..))")
    private void controllerPointcut() {}

    @Around("controllerPointcut()")
    public Object logApiTrace(ProceedingJoinPoint joinPoint) throws Throwable {

        ServletRequestAttributes attributes = (ServletRequestAttributes) Objects.requireNonNull(RequestContextHolder.getRequestAttributes());
        HttpServletRequest request = attributes.getRequest();
        HttpServletResponse response = attributes.getResponse();

        long startTime = System.currentTimeMillis();
        String requestId = MDC.get("request_id");
        String requestURI = request.getRequestURI();
        String httpMethod = request.getMethod();
        String clientIp = request.getRemoteAddr();
        String controllerName = joinPoint.getSignature().getDeclaringType().getSimpleName();
        String methodName = joinPoint.getSignature().getName();

        log.info("API-REQUEST | {} {} | IP: {} | {}.{}",
                httpMethod, requestURI, clientIp, controllerName, methodName);

        try {
            Object result = joinPoint.proceed();
            return result;
        } finally {
            long endTime = System.currentTimeMillis();
            long executionTime = endTime - startTime;
            int statusCode = (response != null) ? response.getStatus() : 0;

            log.info("API-RESPONSE | {} {} | Status: {} | Execution Time: {}ms",
                     httpMethod, requestURI, statusCode,executionTime);
        }
    }
}
