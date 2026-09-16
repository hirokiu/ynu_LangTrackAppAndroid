package com.alchembright.dev.langtrackapp.screen.splash

/*
* Stephan Björck
* Humanistlaboratoriet
* Lunds Universitet
* stephan.bjorck@humlab.lu.se

* Viktor Czyżewski
* RSE Team
* University of York
* */

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import com.alchembright.dev.langtrackapp.R
import com.alchembright.dev.langtrackapp.screen.main.MainActivity

class SplashActivity : AppCompatActivity() {

    companion object{
        private const val DELAY: Long = 1500
    }

    private val mHandler = Handler()
    private val mLauncher = Launcher()

    override fun onStart() {
        super.onStart()
        mHandler.postDelayed(mLauncher, DELAY)
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.splash_activity)

        /*if extras is not null, it contains a notification - save to firebase
        //this is how you handle notification if app is in background and user clicked notification
        if(intent.extras != null && intent.extras is Bundle){
            Notification.saveNewNotification(intent.extras as Bundle)
        }*/
    }

    override fun onStop() {
        mHandler.removeCallbacks(mLauncher)
        super.onStop()
    }

    private fun launch(){
        MainActivity.start(this)
        finish()
    }

    private inner class Launcher: Runnable{
        override fun run() {
            launch()
        }

    }
}
