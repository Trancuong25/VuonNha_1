package com.example.vuonnha.ui

import android.graphics.Color
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.DisposableEffect
import com.example.vuonnha.notify.Notifications
import com.example.vuonnha.ui.nav.AppNav
import com.example.vuonnha.ui.theme.ThemePrefs
import com.example.vuonnha.ui.theme.VuonNhaTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ThemePrefs.load(this)
        Notifications.setup(this)
        setContent {
            val dark = when (ThemePrefs.mode) { 1 -> false; 2 -> true; else -> isSystemInDarkTheme() }
            // Icon thanh trạng thái/điều hướng phải đổi theo theme của app, không theo hệ thống.
            DisposableEffect(dark) {
                enableEdgeToEdge(
                    statusBarStyle = SystemBarStyle.auto(Color.TRANSPARENT, Color.TRANSPARENT) { dark },
                    navigationBarStyle = SystemBarStyle.auto(Color.TRANSPARENT, Color.TRANSPARENT) { dark },
                )
                onDispose { }
            }
            VuonNhaTheme(darkTheme = dark) {
                AppNav()
            }
        }
    }
}
