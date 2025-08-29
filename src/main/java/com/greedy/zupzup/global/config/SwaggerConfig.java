package com.greedy.zupzup.global.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import java.util.List;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@OpenAPIDefinition(
        info = @io.swagger.v3.oas.annotations.info.Info(
                title = "📸ZupZup",
                description = """
                        ### 줍줍(Zupzup)
                        #### [Github](https://github.com/greedy-team/zup-zup-be.git)""",
                version = "1.0v"
        )
)
@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI openAPI() {
        String sejongSessionAuth = "sejongSessionAuth";
        SecurityScheme sejongSessionScheme = new SecurityScheme()
                .name("JSESSIONID")
                .type(SecurityScheme.Type.APIKEY)
                .in(SecurityScheme.In.COOKIE)
                .description("세종대학교 학생 인증 성공 후 발급되는 세션 쿠키");

        String zupzupAccessTokenAuth = "zupzupAccessTokenAuth";
        SecurityScheme accessTokenScheme = new SecurityScheme()
                .name("access_token")
                .type(SecurityScheme.Type.APIKEY)
                .in(SecurityScheme.In.COOKIE)
                .description("서비스 로그인 성공 후 발급되는 Access Token 쿠키");

        List<Server> servers = List.of(
                new Server().url("http://localhost:8080").description("로컬 서버"),
                new Server().url("https://api.sejong-zupzup.kr").description("메인 서버")
        );

        return new OpenAPI()
                .servers(servers)
                .components(new Components()
                        .addSecuritySchemes(sejongSessionAuth, sejongSessionScheme)
                        .addSecuritySchemes(zupzupAccessTokenAuth, accessTokenScheme));
    }
}
