package com.alchembright.dev.langtrackapp.util

import android.app.Activity
import android.content.Intent
import android.widget.TextView
import com.alchembright.dev.langtrackapp.BuildConfig
import com.alchembright.dev.langtrackapp.R
import com.google.android.material.dialog.MaterialAlertDialogBuilder

object ProjectEnvironment {
    val isDev get() = BuildConfig.FLAVOR == "dev"
    private var preferences: android.content.SharedPreferences? = null
    private val cacheKey get() = com.google.firebase.auth.FirebaseAuth.getInstance().currentUser?.uid?.let { BuildConfig.API_BASE_URL + ":" + it }
    val name get() = cacheKey?.let { preferences?.getString(it, null) } ?: if (isDev) "Dev" else "Proto"
    val subtitle get() = cacheKey?.let { preferences?.getString(it + ":subtitle", null) }
    fun initialize(context: android.content.Context) {
        preferences = context.applicationContext.getSharedPreferences("project-display-names", android.content.Context.MODE_PRIVATE)
    }
    fun accept(project: org.json.JSONObject?) {
        if (project?.optString("projectId") != if (isDev) "dev" else "proto") return
        val value = (project.opt("displayName") as? String)?.trim() ?: return
        if (value.isBlank() || value.codePointCount(0, value.length) > 80 || value.any { Character.isISOControl(it) }) return
        val key = cacheKey ?: return
        val edit = preferences?.edit()?.putString(key, value)
        val subtitle = project.opt("subtitle") as? String
        if (subtitle != null && subtitle.codePointCount(0, subtitle.length) <= 120 && subtitle.none { Character.isISOControl(it) })
            edit?.putString(key + ":subtitle", subtitle)
        else edit?.remove(key + ":subtitle")
        edit?.apply()
    }
    fun bindSubtitle(view: TextView) {
        val text = subtitle ?: view.context.getString(R.string.project_description)
        view.text = text
        view.visibility = if (text.isEmpty()) android.view.View.GONE else android.view.View.VISIBLE
    }
    fun bindSelector(view: TextView) {
        initialize(view.context)
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
