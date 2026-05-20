package com.redblade.auth.config;

import com.redblade.auth.filter.JwtAuthenticationFilter;
import com.redblade.auth.handler.AuthenticationEntryPointImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * Spring Security 配置
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final AuthenticationEntryPointImpl authenticationEntryPoint;

    /**
     * 白名单路径（无需认证）
     */
    private static final String[] WHITE_LIST = {
        "/auth/login",              // 登录
        "/auth/register",           // 注册
        "/auth/captcha",            // 验证码
        "/auth/refresh",            // 刷新Token
        "/druid/**",                // Druid 监控
        "/api/druid/**",            // Druid 监控 (API前缀)
        "/doc.html",                // Knife4j 文档
        "/swagger-ui.html",
        "/swagger-ui/**",
        "/v3/api-docs/**",
        "/webjars/**",
        "/favicon.ico",
        "/error"
    };

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            // 禁用 CSRF
            .csrf(AbstractHttpConfigurer::disable)
            // 禁用 Session
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            // 异常处理
            .exceptionHandling(exception -> exception.authenticationEntryPoint(authenticationEntryPoint))
            // 授权配置
            .authorizeHttpRequests(auth -> auth
                .requestMatchers(WHITE_LIST).permitAll()
                .anyRequest().authenticated()
            )
            // 添加 JWT 过滤器
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    /**
     * 密码编码器
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}