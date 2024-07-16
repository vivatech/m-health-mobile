package com.service.mobile.config;

import org.springframework.web.servlet.LocaleResolver;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.Locale;

public class CustomLocaleResolver implements LocaleResolver {
    private static final Locale DEFAULT_LOCALE = Locale.ENGLISH;

    @Override
    public Locale resolveLocale(HttpServletRequest request) {
        String headerLang = request.getHeader("X-localization");
        return (headerLang == null || headerLang.isEmpty()) ? DEFAULT_LOCALE : Locale.forLanguageTag(headerLang);
    }

    @Override
    public void setLocale(HttpServletRequest request, HttpServletResponse response, Locale locale) {

    }
}
