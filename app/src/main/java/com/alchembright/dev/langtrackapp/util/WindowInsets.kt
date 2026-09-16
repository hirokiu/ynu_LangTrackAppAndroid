package com.alchembright.dev.langtrackapp.util

import android.app.Activity
import android.view.View
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat

fun Activity.applySystemBarInsets() {
    WindowCompat.setDecorFitsSystemWindows(window, false)
    val content = findViewById<View>(android.R.id.content)
    val left = content.paddingLeft
    val top = content.paddingTop
    val right = content.paddingRight
    val bottom = content.paddingBottom
    ViewCompat.setOnApplyWindowInsetsListener(content) { view, insets ->
        val bars = insets.getInsets(WindowInsetsCompat.Type.systemBars() or WindowInsetsCompat.Type.displayCutout())
        val keyboard = insets.getInsets(WindowInsetsCompat.Type.ime())
        view.setPadding(left + bars.left, top + bars.top, right + bars.right,
            bottom + maxOf(bars.bottom, keyboard.bottom))
        WindowInsetsCompat.CONSUMED
    }
    ViewCompat.requestApplyInsets(content)
}
