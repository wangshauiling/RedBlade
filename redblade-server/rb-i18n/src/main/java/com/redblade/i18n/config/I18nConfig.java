package com.redblade.i18n.config;

import com.redblade.i18n.resolver.HeaderLocaleResolver;
import lombok.RequiredArgsConstructor;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.ResourceBundleMessageSource;
import org.springframework.web.servlet.LocaleResolver;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.i18n.LocaleChangeInterceptor;

import java.nio.charset.StandardCharsets;

/**
 * 国际化配置
 */
@Configuration
@RequiredArgsConstructor
public class I18nConfig implements WebMvcConfigurer {

    private final HeaderLocaleResolver headerLocaleResolver;

    /**
     * 消息源配置
     */
    @Bean
    public MessageSource messageSource() {
        ResourceBundleMessageSource messageSource = new ResourceBundleMessageSource();
        // 设置资源文件基础名
        messageSource.setBasenames("i18n/messages");
        // 设置默认编码
        messageSource.setDefaultEncoding(StandardCharsets.UTF_8.name());
        // 设置默认语言
        messageSource.setDefaultLocale(java.util.Locale.SIMPLIFIED_CHINESE);
        // 缓存时间（秒），生产环境可设置较长
        messageSource.setCacheSeconds(3600);
        // 找不到消息时返回 key
        messageSource.setUseCodeAsDefaultMessage(true);
        return messageSource;
    }

    /**
     * 语言解析器
     */
    @Bean
    public LocaleResolver localeResolver() {
        return headerLocaleResolver;
    }

    /**
     * 语言切换拦截器（可选，用于 URL 参数切换）
     */
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        LocaleChangeInterceptor interceptor = new LocaleChangeInterceptor();
        interceptor.setParamName("lang");
        registry.addInterceptor(interceptor);
    }
}