package com.ssafy.cholog.global.security.jwt;


import com.ssafy.cholog.global.common.constants.SecurityConstants;
import com.ssafy.cholog.global.common.constants.UserType;
import com.ssafy.cholog.global.security.auth.UserPrincipal;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Collections;
import java.util.Date;

@Component
@RequiredArgsConstructor
public class JwtTokenProvider {

    @Value("${jwt.secret}")
    private String secretKey;

    // SecretKey 생성
    private SecretKey getSigningKey() {
        byte[] keyBytes = Decoders.BASE64.decode(secretKey);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    // 기존 토큰 생성 메소드 유지
    public String createToken(String userId, UserType userType, long validityTime) {
        Date now = new Date();

        return Jwts.builder()
                .subject(userId)
                .claim(SecurityConstants.ROLE_NAME, userType.name())
                .issuedAt(now)
                .expiration(new Date(now.getTime() + validityTime))
                .signWith(getSigningKey())
                .compact();
    }

    // Refresh Token 생성
    public String createAccessToken(String userId, UserType userType) {
        long accessTokenValidTime = SecurityConstants.ACCESS_TOKEN_VALIDITY_SECONDS * 1000L;
        return createToken(userId, userType, accessTokenValidTime);
    }

    // Refresh Token 생성
    public String createRefreshToken(String userId, UserType userType) {
        long refreshTokenValidTime = SecurityConstants.REFRESH_TOKEN_VALIDITY_SECONDS * 1000L;
        return createToken(userId, userType, refreshTokenValidTime);
    }

    // 토큰에서 회원 정보 추출
    public Integer getUserId(String token) {
        return Integer.parseInt(extractAllClaims(token).getSubject());
    }

    public String getUserType(String token) {
        return extractAllClaims(token).get(SecurityConstants.ROLE_NAME, String.class);
    }

    // Claims 추출
    private Claims extractAllClaims(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    // 토큰 유효성 + 만료일자 확인
    public TokenValidationResult validateToken(String token) {
        try {
            extractAllClaims(token);
            return new TokenValidationResult(true, null);
        } catch (ExpiredJwtException e) {
            return new TokenValidationResult(false, TokenError.EXPIRED);
        } catch (Exception e) {
            return new TokenValidationResult(false, TokenError.INVALID);
        }
    }

    // Spring Security 인증 객체 생성
    public Authentication getAuthentication(String token) {
        Integer userId = getUserId(token);
        String userType = getUserType(token);

        UserPrincipal userPrincipal = UserPrincipal.builder()
                .id(userId)
                .authorities(Collections.singletonList(new SimpleGrantedAuthority(SecurityConstants.ROLE_PREFIX + userType)))
                .build();

        return new UsernamePasswordAuthenticationToken(
                userPrincipal,
                "",
                userPrincipal.getAuthorities());
    }
}
