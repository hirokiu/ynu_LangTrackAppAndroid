package com.alchembright.dev.langtrackapp.util

import android.app.Activity
import android.graphics.Color
import android.view.ViewGroup
import android.widget.ImageView
import androidx.core.splashscreen.SplashScreen
import com.alchembright.dev.langtrackapp.R

/** Shared Dev/Proto branding transition after Android's system-owned splash. */
fun Activity.configureLaunchArtwork(splash: SplashScreen, finished: () -> Unit = {}) {
    splash.setOnExitAnimationListener { provider ->
        val root = window.decorView as ViewGroup
        val artwork = ImageView(this).apply {
            setBackgroundColor(Color.rgb(255, 88, 87))
            setImageResource(R.drawable.kirokun_launch_ribbon)
            scaleType = ImageView.ScaleType.FIT_CENTER
            importantForAccessibility = android.view.View.IMPORTANT_FOR_ACCESSIBILITY_NO
            isClickable = true
        }
        root.addView(artwork, ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT))
        provider.remove()
        // Animation only: no sleep, network wait or minimum splash duration.
        artwork.animate().alpha(0f).setDuration(450).withEndAction {
            root.removeView(artwork)
            if (!isFinishing && !isDestroyed) finished()
        }.start()
    }
}
