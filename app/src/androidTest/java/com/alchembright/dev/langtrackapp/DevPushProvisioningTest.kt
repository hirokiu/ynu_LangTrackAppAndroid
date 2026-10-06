package com.alchembright.dev.langtrackapp

import androidx.test.platform.app.InstrumentationRegistry
import com.google.firebase.FirebaseApp
import com.google.firebase.messaging.FirebaseMessaging
import com.google.android.gms.tasks.Tasks
import org.junit.Test
import org.junit.Assert.*
import org.junit.Assume.assumeTrue
import org.json.JSONObject
import java.io.File
import java.util.concurrent.TimeUnit

class DevPushProvisioningTest {
    private val context get() = InstrumentationRegistry.getInstrumentation().targetContext
    private fun requireDev() {
        assumeTrue("Explicit opt-in required", InstrumentationRegistry.getArguments().getString("pushQa") == "true")
        assertTrue(BuildConfig.DEBUG)
        assertEquals("com.alchembright.kirokun.dev", context.packageName)
        assertEquals("kirokun-dev", FirebaseApp.getInstance().options.projectId)
    }
    @Test fun enableSingleDeviceTest() {
        requireDev()
        context.getSharedPreferences("push-qa", 0).edit().putBoolean("enabled", true).commit()
        val messaging = FirebaseMessaging.getInstance()
        messaging.isAutoInitEnabled = true
        val token = Tasks.await(messaging.token, 45, TimeUnit.SECONDS)
        assertTrue(token.isNotBlank())
        File(context.filesDir, "kirokun-push-qa-token.json").writeText(JSONObject().put("projectId", "kirokun-dev")
            .put("bundleId", context.packageName).put("token", token).toString())
    }
    @Test fun disableSingleDeviceTest() {
        requireDev()
        context.getSharedPreferences("push-qa", 0).edit().clear().commit()
        val messaging = FirebaseMessaging.getInstance()
        messaging.isAutoInitEnabled = false
        Tasks.await(messaging.deleteToken(), 45, TimeUnit.SECONDS)
        context.filesDir.listFiles()?.filter { it.name.startsWith("kirokun-push-qa-") }?.forEach { it.delete() }
    }
}
