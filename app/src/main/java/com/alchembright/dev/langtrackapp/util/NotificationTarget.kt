package com.alchembright.dev.langtrackapp.util

import android.content.Context
import android.content.Intent

/** Persist a tap through login; never trust it as authorization. */
object NotificationTarget {
    private val keys = listOf("kirokunAssignmentId", "kirokunUserId", "kirokunEnvironment")
    fun capture(context: Context, intent: Intent?) {
        val id = intent?.getStringExtra(keys[0]) ?: return
        val recipient = intent.getStringExtra(keys[1]) ?: return
        val environment = intent.getStringExtra(keys[2]) ?: return
        if (!id.matches(Regex("[a-fA-F0-9]{24}")) || recipient.isBlank() || environment != if (ProjectEnvironment.isDev) "dev" else "proto") return
        context.getSharedPreferences("notification-target", Context.MODE_PRIVATE).edit()
            .putString("id", id).putString("recipient", recipient).putLong("time", System.currentTimeMillis()).apply()
        keys.forEach { intent.removeExtra(it) }
    }
    fun consume(context: Context, userId: String): String? {
        val prefs = context.getSharedPreferences("notification-target", Context.MODE_PRIVATE)
        val id = prefs.getString("id", null)
        val valid = prefs.getString("recipient", null) == userId && System.currentTimeMillis() - prefs.getLong("time", 0) < 3600000
        prefs.edit().clear().apply()
        return if (valid) id else null
    }
}
