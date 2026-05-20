package com.redblade.auth.controller;

import com.redblade.auth.domain.CaptchaResponse;
import com.redblade.auth.domain.ChangePasswordRequest;
import com.redblade.auth.domain.LoginRequest;
import com.redblade.auth.domain.LoginResponse;
import com.redblade.auth.domain.RegisterRequest;
import com.redblade.auth.service.AuthService;
import com.redblade.auth.service.CaptchaService;
import com.redblade.common.domain.R;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

/**
 * 认证控制器
 */
@Slf4j
@Tag(name = "认证管理")
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final CaptchaService captchaService;

    /**
     * 获取验证码
     */
    @Operation(summary = "获取验证码")
    @GetMapping("/captcha")
    public R<CaptchaResponse> getCaptcha() {
        CaptchaResponse response = captchaService.generateCaptcha();
        return R.ok(response);
    }

    /**
     * 登录
     */
    @Operation(summary = "用户登录")
    @PostMapping("/login")
    public R<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        LoginResponse response = authService.login(request);
        return R.ok(response);
    }

    /**
     * 注册
     */
    @Operation(summary = "用户注册")
    @PostMapping("/register")
    public R<LoginResponse> register(@Valid @RequestBody RegisterRequest request) {
        LoginResponse response = authService.register(request);
        return R.ok(response);
    }

    /**
     * 登出
     */
    @Operation(summary = "用户登出")
    @PostMapping("/logout")
    public R<Void> logout() {
        authService.logout();
        return R.ok();
    }

    /**
     * 刷新令牌
     */
    @Operation(summary = "刷新令牌")
    @PostMapping("/refresh")
    public R<LoginResponse> refresh(@RequestBody String refreshToken) {
        LoginResponse response = authService.refresh(refreshToken);
        return R.ok(response);
    }

    /**
     * 获取当前用户信息
     */
    @Operation(summary = "获取当前用户信息")
    @GetMapping("/userinfo")
    public R<?> getUserInfo() {
        return R.ok(authService.getCurrentUser());
    }

    /**
     * 修改密码
     */
    @Operation(summary = "修改密码")
    @PostMapping("/changePassword")
    public R<Void> changePassword(@Valid @RequestBody ChangePasswordRequest request) {
        authService.changePassword(request);
        return R.ok();
    }
}
