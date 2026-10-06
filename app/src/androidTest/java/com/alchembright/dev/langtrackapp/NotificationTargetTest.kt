package com.alchembright.dev.langtrackapp

import android.content.Intent
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.alchembright.dev.langtrackapp.util.NotificationTarget
import com.alchembright.dev.langtrackapp.util.ProjectEnvironment
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class NotificationTargetTest {
    @Test fun validatesScopeRecipientAndConsumesOnce() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val prefs = context.getSharedPreferences("notification-target", 0)
        assertFalse("Do not overwrite a pending user notification", prefs.contains("id"))
        val id = "0123456789abcdef01234567"
        fun tap(environment: String, assignment: String = id) = Intent()
            .putExtra("kirokunAssignmentId", assignment).putExtra("kirokunUserId", "qa-target")
            .putExtra("kirokunEnvironment", environment)
        val environment = if (ProjectEnvironment.isDev) "dev" else "proto"
        try {
            NotificationTarget.capture(context, tap(environment))
            assertEquals(id, NotificationTarget.consume(context, "qa-target"))
            assertNull(NotificationTarget.consume(context, "qa-target"))
            NotificationTarget.capture(context, tap(environment))
            assertNull(NotificationTarget.consume(context, "other-user"))
            NotificationTarget.capture(context, tap("other-environment"))
            assertNull(NotificationTarget.consume(context, "qa-target"))
            NotificationTarget.capture(context, tap(environment, "https://example.invalid"))
            assertNull(NotificationTarget.consume(context, "qa-target"))
            NotificationTarget.capture(context, tap(environment))
            prefs.edit().putLong("time", 0).commit()
            assertNull(NotificationTarget.consume(context, "qa-target"))
        } finally { prefs.edit().clear().commit() }
    }
}
