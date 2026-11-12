package com.example.vuonnha.ui

import org.junit.Assert.assertEquals
import org.junit.Test
import java.util.Locale

class LocaleCompatibilityTest {
    @Test fun languageTagKeepsVietnameseLocale() {
        val locale = Locale.forLanguageTag("vi-VN")
        assertEquals("vi", locale.language)
        assertEquals("VN", locale.country)
    }
}
