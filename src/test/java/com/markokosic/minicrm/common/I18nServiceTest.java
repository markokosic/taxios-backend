package com.markokosic.minicrm.common;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;

import java.util.Locale;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class I18nServiceTest {

    @Mock
    private MessageSource messageSource;

    @InjectMocks
    private I18nService i18nService;

    @Test
    void getMessage_WithoutArgs_Success() {
        Locale currentLocale = LocaleContextHolder.getLocale();
        when(messageSource.getMessage(eq("test.key"), any(), eq(currentLocale))).thenReturn("Test Message");

        String message = i18nService.getMessage("test.key");

        assertEquals("Test Message", message);
    }

    @Test
    void getMessage_WithArgs_Success() {
        Locale currentLocale = LocaleContextHolder.getLocale();
        Object[] args = new Object[]{"Arg1"};
        when(messageSource.getMessage(eq("test.key.args"), eq(args), eq(currentLocale))).thenReturn("Test Message Arg1");

        String message = i18nService.getMessage("test.key.args", args);

        assertEquals("Test Message Arg1", message);
    }
}
