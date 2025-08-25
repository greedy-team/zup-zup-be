package com.greedy.zupzup.auth.jwt;

import com.greedy.zupzup.auth.exception.AuthException;
import com.greedy.zupzup.global.exception.ApplicationException;
import com.greedy.zupzup.member.domain.Member;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SignatureException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Date;

@Component
public class JwtTokenProvider {

    private final String accessSecretKey;
    public final int accessExpirationSecond;

    public JwtTokenProvider(@Value("${zupzup.auth.jwt.access.secret}") String accessSecretKey,
                            @Value("${zupzup.auth.jwt.access.expiration-second}") int accessExpirationSecond) {
        this.accessSecretKey = accessSecretKey;
        this.accessExpirationSecond = accessExpirationSecond;
    }


    public String createAccessToken(Member member) {
        Date now = new Date();
        Date expirationTime = new Date(now.getTime() + (accessExpirationSecond * 1000L));

        return Jwts.builder()
                .setSubject(member.getId().toString())
                .setIssuedAt(now)
                .setExpiration(expirationTime)
                .signWith(Keys.hmacShaKeyFor(accessSecretKey.getBytes()))
                .compact();
    }


    public Long getLoginMemberId(String accessToken) {
        return Long.valueOf(
                getAccessClaims(accessToken)
                        .getSubject()
        );
    }


    private Claims getAccessClaims(String accessToken) {
        try {
            return Jwts.parserBuilder()
                    .setSigningKey(Keys.hmacShaKeyFor(accessSecretKey.getBytes()))
                    .build()
                    .parseClaimsJws(accessToken)
                    .getBody();
        } catch (ExpiredJwtException e) {
            throw new ApplicationException(AuthException.ACCESS_TOKEN_EXPIRED);
        } catch (IllegalArgumentException e) {
            throw new ApplicationException(AuthException.UNAUTHENTICATED_REQUEST);
        } catch (JwtException e) {
            throw new ApplicationException(AuthException.INVALID_ACCESS_TOKEN);
        }
    }

}
