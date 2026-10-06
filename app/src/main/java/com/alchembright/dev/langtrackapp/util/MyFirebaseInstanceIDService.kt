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
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.alchembright.dev.langtrackapp.R
import com.alchembright.dev.langtrackapp.data.RepositoryFactory
import com.alchembright.dev.langtrackapp.screen.main.MainActivity

class MyFirebaseInstanceIDService: FirebaseMessagingService() {
    companion object {
        const val MESSAGE_TEXT = "messageText"
        fun getDeviceTokengetDeviceToken(callback: (String?) -> Unit) {
            FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
                callback(if (task.isSuccessful) task.result else null)
            }
        }
    }
    override fun onNewToken(token: String) {
        // Repository resolves identity before updating the device registration.
        RepositoryFactory.getRepository(this).putDeviceToken()
    }
    override fun onMessageReceived(message: RemoteMessage) {
        if (ProjectEnvironment.isDev) return
        if (FirebaseAuth.getInstance().currentUser != null) {
            RepositoryFactory.getRepository(this).getAssignments()
        }
        val title = message.notification?.title ?: getString(R.string.app_name)
        val body = message.notification?.body ?: message.data[MESSAGE_TEXT] ?: return
        KirokunNotifications.show(this, title, body, message.messageId ?: java.util.UUID.randomUUID().toString(), message.data)
    }
}

internal object KirokunNotifications {
    const val CHANNEL_ID = "kirokun_surveys"
    fun ensureChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= 26) {
            context.getSystemService(NotificationManager::class.java).createNotificationChannel(
                NotificationChannel(CHANNEL_ID, context.getString(R.string.notification_channel_surveys), NotificationManager.IMPORTANCE_DEFAULT))
        }
    }
    fun build(context: Context, title: String, body: String, data: Map<String, String> = emptyMap()): android.app.Notification {
        ensureChannel(context)
        val intent = Intent(context, MainActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
        data.filterKeys { it in listOf("kirokunAssignmentId", "kirokunUserId", "kirokunEnvironment") }.forEach { (key, value) -> intent.putExtra(key, value) }
        intent.data = android.net.Uri.parse("kirokun://notification/" + java.util.UUID.randomUUID().toString())
        val action = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
        return NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.kirokun_notification)
            .setColor(ContextCompat.getColor(context, R.color.kirokun_brand))
            .setContentTitle(title).setContentText(body)
            .setStyle(NotificationCompat.BigTextStyle().bigText(body))
            .setContentIntent(action).setAutoCancel(true)
            .setVisibility(NotificationCompat.VISIBILITY_PRIVATE)
            .setDefaults(NotificationCompat.DEFAULT_SOUND)
            .build()
    }
    fun show(context: Context, title: String, body: String, messageId: String, data: Map<String, String> = emptyMap()) {
        if (Build.VERSION.SDK_INT >= 33 && ContextCompat.checkSelfPermission(context,
                android.Manifest.permission.POST_NOTIFICATIONS) != android.content.pm.PackageManager.PERMISSION_GRANTED) return
        val manager = NotificationManagerCompat.from(context)
        if (!manager.areNotificationsEnabled()) return
        try { manager.notify(messageId, 0, build(context, title, body, data)) }
        catch (_: SecurityException) { /* Permission may have been revoked while receiving. */ }
    }
}
