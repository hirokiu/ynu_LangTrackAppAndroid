package com.alchembright.dev.langtrackapp.screen.splash

import android.os.Bundle
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.alchembright.dev.langtrackapp.screen.main.MainActivity

/** Route independently of system splash callbacks, which may be absent on warm starts. */
class SplashActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        val splash = installSplashScreen()
        super.onCreate(savedInstanceState)
        com.alchembright.dev.langtrackapp.util.NotificationTarget.capture(this, intent)
        splash.setKeepOnScreenCondition { true }
        startActivity(Intent(this, MainActivity::class.java).putExtra("showLaunchArtwork", true))
        finish()
    }
}
