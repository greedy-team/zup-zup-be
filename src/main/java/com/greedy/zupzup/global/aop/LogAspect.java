package com.greedy.zupzup.global.aop;

import jakarta.servlet.http.HttpServletRequest;
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

        HttpServletRequest request = ((ServletRequestAttributes) Objects.requireNonNull(RequestContextHolder.getRequestAttributes())).getRequest();

        long startTime = System.currentTimeMillis();
        String requestId = MDC.get("request_id");
        String requestURI = request.getRequestURI();
        String httpMethod = request.getMethod();
        String clientIp = request.getRemoteAddr();
        String controllerName = joinPoint.getSignature().getDeclaringType().getSimpleName();
        String methodName = joinPoint.getSignature().getName();

        log.info("[{}] API-REQUEST | {} {} | IP: {} | {}.{}",
                requestId, httpMethod, requestURI, clientIp, controllerName, methodName);

        try {
            Object result = joinPoint.proceed();
            return result;
        } finally {
            long endTime = System.currentTimeMillis();
            long executionTime = endTime - startTime;

            log.info("[{}] API-RESPONSE | {} {} | Execution Time: {}ms",
                    requestId, httpMethod, requestURI, executionTime);
        }
    }
}
