package com.redblade.auth.aspect;

import com.redblade.auth.annotation.Logical;
import com.redblade.auth.annotation.RequiresPermission;
import com.redblade.auth.annotation.RequiresRole;
import com.redblade.auth.security.LoginUser;
import com.redblade.common.exception.BusinessException;
import com.redblade.common.helper.MessageHelper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Set;

/**
 * 权限校验切面
 */
@Slf4j
@Aspect
@Component
@RequiredArgsConstructor
public class PermissionAspect {

    private final MessageHelper messageHelper;

    /**
     * 权限校验
     */
    @Around("@annotation(com.redblade.auth.annotation.RequiresPermission)")
    public Object checkPermission(ProceedingJoinPoint joinPoint) throws Throwable {
        // 1. 获取注解
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        RequiresPermission annotation = method.getAnnotation(RequiresPermission.class);

        // 2. 获取当前用户
        LoginUser loginUser = getLoginUser();
        if (loginUser == null) {
            throw new BusinessException(messageHelper.get("user.not.login"));
        }

        // 3. 获取用户权限
        Set<String> userPermissions = loginUser.getPermissions();
        if (userPermissions == null || userPermissions.isEmpty()) {
            throw new BusinessException(messageHelper.get("user.no.permission"));
        }

        // 4. 校验权限
        String[] requiredPermissions = annotation.value();
        Logical logical = annotation.logical();

        boolean hasPermission;
        if (logical == Logical.AND) {
            // 必须具有所有权限
            hasPermission = Arrays.stream(requiredPermissions)
                .allMatch(userPermissions::contains);
        } else {
            // 只需具有其中一个权限
            hasPermission = Arrays.stream(requiredPermissions)
                .anyMatch(userPermissions::contains);
        }

        if (!hasPermission) {
            log.warn("用户 {} 无权限访问: {}", loginUser.getUsername(), Arrays.toString(requiredPermissions));
            throw new BusinessException(messageHelper.get("user.no.permission"));
        }

        return joinPoint.proceed();
    }

    /**
     * 角色校验
     */
    @Around("@annotation(com.redblade.auth.annotation.RequiresRole)")
    public Object checkRole(ProceedingJoinPoint joinPoint) throws Throwable {
        // 1. 获取注解
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        RequiresRole annotation = method.getAnnotation(RequiresRole.class);

        // 2. 获取当前用户
        LoginUser loginUser = getLoginUser();
        if (loginUser == null) {
            throw new BusinessException(messageHelper.get("user.not.login"));
        }

        // 3. 获取用户角色
        Set<String> userRoles = loginUser.getRoles();
        if (userRoles == null || userRoles.isEmpty()) {
            throw new BusinessException(messageHelper.get("user.no.permission"));
        }

        // 4. 校验角色
        String[] requiredRoles = annotation.value();
        Logical logical = annotation.logical();

        boolean hasRole;
        if (logical == Logical.AND) {
            hasRole = Arrays.stream(requiredRoles)
                .allMatch(userRoles::contains);
        } else {
            hasRole = Arrays.stream(requiredRoles)
                .anyMatch(userRoles::contains);
        }

        if (!hasRole) {
            log.warn("用户 {} 无角色访问: {}", loginUser.getUsername(), Arrays.toString(requiredRoles));
            throw new BusinessException(messageHelper.get("user.no.permission"));
        }

        return joinPoint.proceed();
    }

    /**
     * 获取当前登录用户
     */
    private LoginUser getLoginUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof LoginUser) {
            return (LoginUser) authentication.getPrincipal();
        }
        return null;
    }
}
