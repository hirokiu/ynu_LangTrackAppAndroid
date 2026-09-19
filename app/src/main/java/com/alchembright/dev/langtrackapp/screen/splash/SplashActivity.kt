package com.alchembright.dev.langtrackapp.screen.splash

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.alchembright.dev.langtrackapp.screen.main.MainActivity

/** Routing only: the system owns the launch screen; no artificial delay. */
class SplashActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        val splash = installSplashScreen()
        super.onCreate(savedInstanceState)
        splash.setKeepOnScreenCondition { true }
        MainActivity.start(this)
        finish()
    }
}
