package com.redblade.i18n.service.impl;

import com.redblade.i18n.constant.LangConstants;
import com.redblade.i18n.service.I18nService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.Locale;

/**
 * 国际化服务实现
 */
@Service
@RequiredArgsConstructor
public class I18nServiceImpl implements I18nService {

    private final MessageSource messageSource;

    @Override
    public String getCurrentLang() {
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes != null) {
            HttpServletRequest request = attributes.getRequest();
            String lang = request.getHeader(LangConstants.HEADER_ACCEPT_LANGUAGE);
            return lang != null ? lang : LangConstants.DEFAULT_LANG;
        }
        return LangConstants.DEFAULT_LANG;
    }

    @Override
    public String getMessage(String code) {
        return getMessage(code, new Object[]{});
    }

    @Override
    public String getMessage(String code, Object... args) {
        return getMessageByLang(code, args, getCurrentLang());
    }

    @Override
    public String getMessageByLang(String code, String lang) {
        return getMessageByLang(code, new Object[]{}, lang);
    }

    @Override
    public String getMessageByLang(String code, Object[] args, String lang) {
        try {
            Locale locale = parseLocale(lang);
            return messageSource.getMessage(code, args, locale);
        } catch (Exception e) {
            return code;
        }
    }

    @Override
    public String getMessageWithDefault(String code, String defaultMessage) {
        try {
            Locale locale = parseLocale(getCurrentLang());
            return messageSource.getMessage(code, null, defaultMessage, locale);
        } catch (Exception e) {
            return defaultMessage;
        }
    }

    /**
     * 解析语言代码为 Locale
     */
    private Locale parseLocale(String lang) {
        if (lang == null || lang.isEmpty()) {
            return Locale.SIMPLIFIED_CHINESE;
        }
        String[] parts = lang.split("-");
        if (parts.length == 2) {
            return new Locale(parts[0], parts[1]);
        }
        return new Locale(parts[0]);
    }
}