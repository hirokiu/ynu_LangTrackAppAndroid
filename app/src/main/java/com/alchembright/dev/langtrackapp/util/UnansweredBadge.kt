package com.alchembright.dev.langtrackapp.util

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.alchembright.dev.langtrackapp.R
import com.alchembright.dev.langtrackapp.data.model.Assignment
import com.alchembright.dev.langtrackapp.screen.main.MainActivity

object UnansweredBadge {
    private const val CHANNEL = "kirokun_unanswered_count"
    private const val TAG = "unanswered-count"
    fun count(items: List<Assignment>, now: Long = System.currentTimeMillis()): Int = items.filter {
        val published = runCatching { it.publishAt.toDate()?.time }.getOrNull()
        val expiry = runCatching { it.expireAt.toDate()?.time }.getOrNull()
        it.dataset == null && published != null && expiry != null && published <= now && now < expiry
    }.map { it.id }.distinct().size

    fun update(context: Context, count: Int) {
        val manager = NotificationManagerCompat.from(context)
        if (Build.VERSION.SDK_INT >= 26) {
            context.getSystemService(NotificationManager::class.java).createNotificationChannel(
                NotificationChannel(CHANNEL, context.getString(R.string.unanswered_badge_channel), NotificationManager.IMPORTANCE_LOW).apply {
                    setShowBadge(true); setSound(null, null); enableVibration(false)
                })
        }
        // List refresh supersedes stale individual survey notifications and their counts.
        if (Build.VERSION.SDK_INT >= 26) context.getSystemService(NotificationManager::class.java).activeNotifications
            .filter { it.notification.channelId == KirokunNotifications.CHANNEL_ID }
            .forEach { manager.cancel(it.tag, it.id) }
        if (count <= 0) { manager.cancel(TAG, 0); return }
        if (!manager.areNotificationsEnabled()) return
        if (Build.VERSION.SDK_INT >= 33 && ContextCompat.checkSelfPermission(context, android.Manifest.permission.POST_NOTIFICATIONS) != android.content.pm.PackageManager.PERMISSION_GRANTED) return
        val intent = Intent(context, MainActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
        val action = PendingIntent.getActivity(context, 721, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
        val notification = NotificationCompat.Builder(context, CHANNEL)
            .setSmallIcon(R.drawable.kirokun_notification).setContentTitle(context.getString(R.string.unanswered_badge_title, count))
            .setContentText(context.getString(R.string.unanswered_badge_body)).setNumber(count)
            .setContentIntent(action).setOnlyAlertOnce(true).setSilent(true)
            .setVisibility(NotificationCompat.VISIBILITY_PRIVATE).build()
        try { manager.notify(TAG, 0, notification) } catch (_: SecurityException) { }
    }
}
