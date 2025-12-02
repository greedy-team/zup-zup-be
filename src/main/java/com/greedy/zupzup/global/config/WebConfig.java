package com.greedy.zupzup.global.config;

import com.greedy.zupzup.auth.presentation.argumentresolver.LoginMemberArgumentResolver;
import com.greedy.zupzup.auth.presentation.interceptor.AdminInterceptor;
import com.greedy.zupzup.auth.presentation.interceptor.AuthInterceptor;
import com.greedy.zupzup.global.presentation.interceptor.LogInterceptor;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.List;

@Configuration
@RequiredArgsConstructor
public class WebConfig implements WebMvcConfigurer {

    private final LoginMemberArgumentResolver loginMemberArgumentResolver;
    private final LogInterceptor logInterceptor;
    private final AdminInterceptor adminInterceptor;
    private final AuthInterceptor authInterceptor;

    @Value("${cors.allowed-origins}")
    private List<String> allowedOrigins;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {

        registry.addInterceptor(logInterceptor)
                .addPathPatterns("/api/**")
                .order(1);

        registry.addInterceptor(adminInterceptor)
                .addPathPatterns("/api/admin/**")
                .order(2);

        registry.addInterceptor(authInterceptor)
                .addPathPatterns("/api/**")
                .excludePathPatterns(
                        "/api/auth/**",
                        "/api/lost-items/summary",
                        "/api/lost-items",
                        "/api/lost-items/*",
                        "/api/school-areas/**",
                        "/api/categories/**",
                        "/api/admin/**"
                )
                .order(3);
    }

    @Override
    public void addArgumentResolvers(List<HandlerMethodArgumentResolver> resolvers) {
        resolvers.add(loginMemberArgumentResolver);
    }

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/api/**")
                .allowedOrigins(allowedOrigins.toArray(new String[0]))
                .allowedMethods("*")
                .allowedHeaders("*")
                .allowCredentials(true)
                .maxAge(3000);
    }
}
