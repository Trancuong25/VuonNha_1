package com.example.vuonnha.ui.theme

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.setValue

/** Chế độ giao diện: 0 = theo hệ thống, 1 = sáng, 2 = tối. Lưu trong SharedPreferences. */
object ThemePrefs {
    var mode by mutableIntStateOf(0)
        private set

    fun load(ctx: Context) { mode = ctx.getSharedPreferences("settings", Context.MODE_PRIVATE).getInt("theme", 0) }

    fun set(ctx: Context, m: Int) {
        mode = m
        ctx.getSharedPreferences("settings", Context.MODE_PRIVATE).edit().putInt("theme", m).apply()
    }
}
