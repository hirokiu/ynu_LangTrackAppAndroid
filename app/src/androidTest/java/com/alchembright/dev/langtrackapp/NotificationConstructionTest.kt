package com.alchembright.dev.langtrackapp

import android.app.Notification
import android.app.NotificationManager
import android.os.Build
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.alchembright.dev.langtrackapp.util.KirokunNotifications
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith

/** Constructs standard notifications without posting them or contacting FCM. */
@RunWith(AndroidJUnit4::class)
class NotificationConstructionTest {
    @Test fun tapActionAndPrivateJapaneseContent() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val notification = KirokunNotifications.build(context, "アンケートのお知らせ", "回答できます")
        assertEquals("アンケートのお知らせ", notification.extras.getString(Notification.EXTRA_TITLE))
        assertEquals("回答できます", notification.extras.getString(Notification.EXTRA_TEXT))
        assertEquals(Notification.VISIBILITY_PRIVATE, notification.visibility)
        assertTrue(notification.flags and Notification.FLAG_AUTO_CANCEL != 0)
        assertNotNull(notification.contentIntent)
        assertEquals(context.packageName, notification.contentIntent.creatorPackage)
        if (Build.VERSION.SDK_INT >= 31) {
            assertTrue(notification.contentIntent.isImmutable)
            assertTrue(notification.contentIntent.isActivity)
        }
        if (Build.VERSION.SDK_INT >= 26) {
            assertEquals(KirokunNotifications.CHANNEL_ID, notification.channelId)
            val channel = context.getSystemService(NotificationManager::class.java).getNotificationChannel(notification.channelId)
            assertNotNull(channel)
            assertEquals(NotificationManager.IMPORTANCE_DEFAULT, channel.importance)
            // Recreating the channel must preserve the user's disabled setting.
            channel.importance = NotificationManager.IMPORTANCE_NONE
            context.getSystemService(NotificationManager::class.java).createNotificationChannel(channel)
            KirokunNotifications.ensureChannel(context)
            assertEquals(NotificationManager.IMPORTANCE_NONE,
                context.getSystemService(NotificationManager::class.java).getNotificationChannel(notification.channelId).importance)
        }
    }
}
