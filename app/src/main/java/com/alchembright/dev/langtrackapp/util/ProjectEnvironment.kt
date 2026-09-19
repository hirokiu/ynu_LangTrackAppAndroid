package com.alchembright.dev.langtrackapp.util

import android.app.Activity
import android.content.Intent
import android.widget.TextView
import com.alchembright.dev.langtrackapp.BuildConfig
import com.alchembright.dev.langtrackapp.R
import com.google.android.material.dialog.MaterialAlertDialogBuilder

object ProjectEnvironment {
    val isDev get() = BuildConfig.FLAVOR == "dev"
    val name get() = if (isDev) "Dev" else "Proto"
    fun bindSelector(view: TextView) {
        view.text = view.context.getString(R.string.project_label, name) + " ▾"
        view.setOnClickListener {
            MaterialAlertDialogBuilder(view.context)
                .setTitle(R.string.project_switch)
                .setSingleChoiceItems(arrayOf(name), 0) { dialog, _ -> dialog.dismiss() }
                .setPositiveButton(android.R.string.ok, null).show()
        }
    }
    fun openLogin(activity: Activity) {
        val target = if (isDev) "com.alchembright.kirokun.dev.DevSetupActivity"
            else "com.alchembright.dev.langtrackapp.screen.login.LoginActivity"
        activity.startActivity(Intent().setClassName(activity, target))
    }
}
