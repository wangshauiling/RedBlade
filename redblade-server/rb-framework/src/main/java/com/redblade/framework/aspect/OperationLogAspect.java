package com.redblade.framework.aspect;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

/**
 * 操作日志切面
 */
@Slf4j
@Aspect
@Component
public class OperationLogAspect {

    private static final ThreadLocal<Long> startTime = new ThreadLocal<>();

    /**
     * 前置通知
     */
    @Before("execution(* com.redblade..controller..*.*(..))")
    public void doBefore(JoinPoint joinPoint) {
        startTime.set(System.currentTimeMillis());

        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes != null) {
            HttpServletRequest request = attributes.getRequest();
            log.info("请求开始 => {} {} - {}.{}",
                request.getMethod(),
                request.getRequestURI(),
                joinPoint.getTarget().getClass().getSimpleName(),
                joinPoint.getSignature().getName());
        }
    }

    /**
     * 返回通知
     */
    @AfterReturning(pointcut = "execution(* com.redblade..controller..*.*(..))", returning = "result")
    public void doAfterReturning(JoinPoint joinPoint, Object result) {
        long cost = System.currentTimeMillis() - startTime.get();
        log.info("请求结束 => 耗时: {}ms", cost);
        startTime.remove();
    }

    /**
     * 异常通知
     */
    @AfterThrowing(pointcut = "execution(* com.redblade..controller..*.*(..))", throwing = "e")
    public void doAfterThrowing(JoinPoint joinPoint, Throwable e) {
        long cost = System.currentTimeMillis() - startTime.get();
        log.error("请求异常 => {}.{} - 耗时: {}ms - 异常: {}",
            joinPoint.getTarget().getClass().getSimpleName(),
            joinPoint.getSignature().getName(),
            cost,
            e.getMessage());
        startTime.remove();
    }
}