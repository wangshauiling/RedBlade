package com.redblade.auth.filter;

import com.redblade.auth.jwt.JwtUtils;
import com.redblade.auth.security.LoginUser;
import com.redblade.common.constant.CacheConstants;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

/**
 * JWT 认证过滤器
 * 从请求头获取 Token，验证并设置用户认证信息
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtUtils jwtUtils;
    private final RedisTemplate<String, Object> redisTemplate;

    private static final String AUTHORIZATION_HEADER = "Authorization";
    private static final String BEARER_PREFIX = "Bearer ";

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
        throws ServletException, IOException {

        // 1. 从请求头获取 Token
        String token = resolveToken(request);

        if (StringUtils.hasText(token)) {
            try {
                // 2. 验证 Token
                if (jwtUtils.validateToken(token) && jwtUtils.isAccessToken(token)) {
                    // 3. 从 Redis 获取用户信息
                    String cacheKey = CacheConstants.LOGIN_TOKEN_KEY + token;
                    LoginUser loginUser = (LoginUser) redisTemplate.opsForValue().get(cacheKey);

                    if (loginUser != null) {
                        // 4. 刷新 Token 过期时间（滑动过期）
                        redisTemplate.expire(cacheKey, 2, TimeUnit.HOURS);

                        // 5. 设置认证信息到 SecurityContext
                        UsernamePasswordAuthenticationToken authentication =
                            new UsernamePasswordAuthenticationToken(loginUser, null, loginUser.getAuthorities());
                        SecurityContextHolder.getContext().setAuthentication(authentication);

                        log.debug("用户认证成功: {} ({})", loginUser.getUsername(), loginUser.getOrgCode());
                    } else {
                        log.debug("Token 已过期或无效");
                    }
                }
            } catch (Exception e) {
                log.error("Token 验证失败: {}", e.getMessage());
            }
        }

        filterChain.doFilter(request, response);
    }

    /**
     * 从请求头获取 Token
     */
    private String resolveToken(HttpServletRequest request) {
        String bearerToken = request.getHeader(AUTHORIZATION_HEADER);
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith(BEARER_PREFIX)) {
            return bearerToken.substring(BEARER_PREFIX.length());
        }
        return null;
    }
}
