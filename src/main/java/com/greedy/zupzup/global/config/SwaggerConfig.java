package com.greedy.zupzup.global.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI openAPI() {
        Info info = new Info()
                .title("\uD83D\uDCF7ZupZup")
                .description("""
                        ### 줍줍(Zupzup)
                        #### [Github](https://github.com/greedy-team/zup-zup-be.git)""")
                .version("v1.0.0");

        // 1. 세션 쿠키 인증(JSESSIONID)을 위한 SecurityScheme 설정 (세종대학교 인증 시)
        String sessionIdSchemeName = "sejongSessionAuth";

        // 2. Access Token 쿠키 인증을 위한 SecurityScheme 설정 (줍줍 서비스 인증 시)
        String accessTokenSchemeName = "zupzupAccessTokenAuth";


        Components components = new Components()
                .addSecuritySchemes(sessionIdSchemeName, new SecurityScheme()
                        .name("JSESSIONID")
                        .type(SecurityScheme.Type.APIKEY)
                        .in(SecurityScheme.In.COOKIE)
                        .description("세종대학교 학생 인증 성공 후 발급되는 세션 쿠키"))

                .addSecuritySchemes(accessTokenSchemeName, new SecurityScheme()
                        .name("access_token")
                        .type(SecurityScheme.Type.APIKEY)
                        .in(SecurityScheme.In.COOKIE)
                        .description("서비스 로그인 성공 후 발급되는 Access Token 쿠키"));


        return new OpenAPI()
                .info(info)
                .components(components);
    }
}
