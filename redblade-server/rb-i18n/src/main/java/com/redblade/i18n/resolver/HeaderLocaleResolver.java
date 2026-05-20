package com.redblade.i18n.resolver;

import com.redblade.i18n.constant.LangConstants;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.i18n.AcceptHeaderLocaleResolver;

import java.util.Arrays;
import java.util.Locale;

/**
 * 请求头语言解析器
 * 从 Accept-Language 请求头获取语言设置
 */
@Component
public class HeaderLocaleResolver extends AcceptHeaderLocaleResolver {

    public HeaderLocaleResolver() {
        // 设置支持的语言列表
        setSupportedLocales(Arrays.asList(
            Locale.SIMPLIFIED_CHINESE,  // zh-CN
            Locale.TRADITIONAL_CHINESE, // zh-TW
            Locale.US,                  // en-US
            Locale.JAPAN,               // ja-JP
            Locale.KOREA,               // ko-KR
            new Locale("vi", "VN")      // vi-VN
        ));
        // 设置默认语言
        setDefaultLocale(Locale.SIMPLIFIED_CHINESE);
    }

    @Override
    public Locale resolveLocale(HttpServletRequest request) {
        String lang = request.getHeader(LangConstants.HEADER_ACCEPT_LANGUAGE);

        if (lang != null && !lang.isEmpty()) {
            try {
                // 转换语言代码为 Locale
                String[] parts = lang.split("-");
                Locale locale;
                if (parts.length == 2) {
                    locale = new Locale(parts[0], parts[1]);
                } else {
                    locale = new Locale(parts[0]);
                }

                // 检查是否支持
                if (getSupportedLocales().contains(locale)) {
                    return locale;
                }
            } catch (Exception e) {
                // 解析失败，使用默认语言
            }
        }

        return getDefaultLocale();
    }
}