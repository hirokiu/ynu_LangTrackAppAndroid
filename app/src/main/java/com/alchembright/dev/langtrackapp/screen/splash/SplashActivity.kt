package com.alchembright.dev.langtrackapp.screen.splash

import android.os.Bundle
import com.alchembright.dev.langtrackapp.util.configureLaunchArtwork
import androidx.appcompat.app.AppCompatActivity
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.alchembright.dev.langtrackapp.screen.main.MainActivity

/** System splash followed by the shared branding exit animation. */
class SplashActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        val splash = installSplashScreen()
        super.onCreate(savedInstanceState)
        com.alchembright.dev.langtrackapp.util.NotificationTarget.capture(this, intent)
        setContentView(com.alchembright.dev.langtrackapp.R.layout.splash_activity)
        configureLaunchArtwork(splash) {
            MainActivity.start(this)
            finish()
        }
    }
}
