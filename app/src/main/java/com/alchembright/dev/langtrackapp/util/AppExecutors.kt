package com.alchembright.dev.langtrackapp.util

import android.os.Handler
import android.os.Looper
import java.util.concurrent.Executor
import java.util.concurrent.Executors

val IO: Executor = Executors.newFixedThreadPool(2)
val UI: Executor = UiThreadExecutor()

internal class UiThreadExecutor : Executor {
    private val mHandler = Handler(Looper.getMainLooper())

    override fun execute(command: Runnable) {
        mHandler.post(command)
    }
}