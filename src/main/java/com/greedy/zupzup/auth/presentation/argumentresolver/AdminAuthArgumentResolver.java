package com.greedy.zupzup.auth.presentation.argumentresolver;

import com.greedy.zupzup.auth.exception.AuthException;
import com.greedy.zupzup.auth.jwt.JwtTokenProvider;
import com.greedy.zupzup.auth.presentation.annotation.AdminAuth;
import com.greedy.zupzup.global.exception.ApplicationException;
import com.greedy.zupzup.global.util.CookieUtil;
import com.greedy.zupzup.member.domain.Member;
import com.greedy.zupzup.member.domain.Role;
import com.greedy.zupzup.member.repository.MemberRepository;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.core.MethodParameter;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

@Component
@RequiredArgsConstructor
public class AdminAuthArgumentResolver implements HandlerMethodArgumentResolver {

    private final JwtTokenProvider jwtTokenProvider;
    private final MemberRepository memberRepository;

    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        return parameter.getParameterAnnotation(AdminAuth.class) != null
                && parameter.getParameterType().equals(LoginAdmin.class);
    }

    @Override
    public Object resolveArgument(MethodParameter parameter,
                                  ModelAndViewContainer mavContainer,
                                  NativeWebRequest webRequest,
                                  WebDataBinderFactory binderFactory) {

        HttpServletRequest request = webRequest.getNativeRequest(HttpServletRequest.class);
        String accessToken = CookieUtil.extractToken(request.getCookies());

        Long memberId = jwtTokenProvider.getLoginMemberId(accessToken);
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new ApplicationException(AuthException.MEMBER_NOT_FOUND));

        if (member.getRole() != Role.ADMIN) {
            throw new ApplicationException(AuthException.FORBIDDEN_ADMIN_ONLY);
        }

        return new LoginAdmin(member.getId());
    }
}
