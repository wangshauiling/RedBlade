package com.redblade.auth.jwt;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.Map;

/**
 * JWT 工具类
 */
@Slf4j
@Component
public class JwtUtils {

    @Value("${rb.jwt.secret:RedBladeSecretKeyForJwtTokenGenerationAndValidation2024}")
    private String secret;

    @Value("${rb.jwt.access-token-expiration:7200000}")
    private long accessTokenExpiration; // 默认2小时

    @Value("${rb.jwt.refresh-token-expiration:604800000}")
    private long refreshTokenExpiration; // 默认7天

    @Value("${rb.jwt.issuer:RedBlade}")
    private String issuer;

    private SecretKey getSecretKey() {
        return Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * 生成 AccessToken
     */
    public String generateAccessToken(String userCode, String username) {
        return generateToken(userCode, username, accessTokenExpiration, "access");
    }

    /**
     * 生成 RefreshToken
     */
    public String generateRefreshToken(String userCode, String username) {
        return generateToken(userCode, username, refreshTokenExpiration, "refresh");
    }

    /**
     * 生成 Token
     */
    private String generateToken(String userCode, String username, long expiration, String type) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + expiration);

        return Jwts.builder()
            .subject(userCode)
            .claim("username", username)
            .claim("type", type)
            .issuer(issuer)
            .issuedAt(now)
            .expiration(expiryDate)
            .signWith(getSecretKey())
            .compact();
    }

    /**
     * 解析 Token
     */
    public Claims parseToken(String token) {
        try {
            return Jwts.parser()
                .verifyWith(getSecretKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
        } catch (Exception e) {
            log.error("解析Token失败: {}", e.getMessage());
            return null;
        }
    }

    /**
     * 从 Token 获取用户编码
     */
    public String getUserCode(String token) {
        Claims claims = parseToken(token);
        if (claims != null) {
            return claims.getSubject();
        }
        return null;
    }

    /**
     * 从 Token 获取用户名
     */
    public String getUsername(String token) {
        Claims claims = parseToken(token);
        if (claims != null) {
            return claims.get("username", String.class);
        }
        return null;
    }

    /**
     * 验证 Token 是否有效
     */
    public boolean validateToken(String token) {
        try {
            Claims claims = parseToken(token);
            if (claims == null) {
                return false;
            }
            // 检查是否过期
            return !claims.getExpiration().before(new Date());
        } catch (Exception e) {
            log.error("验证Token失败: {}", e.getMessage());
            return false;
        }
    }

    /**
     * 检查是否为 AccessToken
     */
    public boolean isAccessToken(String token) {
        Claims claims = parseToken(token);
        if (claims != null) {
            return "access".equals(claims.get("type", String.class));
        }
        return false;
    }

    /**
     * 检查是否为 RefreshToken
     */
    public boolean isRefreshToken(String token) {
        Claims claims = parseToken(token);
        if (claims != null) {
            return "refresh".equals(claims.get("type", String.class));
        }
        return false;
    }
}