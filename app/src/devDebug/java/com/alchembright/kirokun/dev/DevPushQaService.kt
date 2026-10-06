package com.alchembright.kirokun.dev

import com.google.firebase.FirebaseApp
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.alchembright.dev.langtrackapp.util.KirokunNotifications
import java.io.File
import org.json.JSONObject

/** Debug-only, explicitly provisioned single-device push verification. */
class DevPushQaService : FirebaseMessagingService() {
    override fun onMessageReceived(message: RemoteMessage) {
        if (!getSharedPreferences("push-qa", MODE_PRIVATE).getBoolean("enabled", false)) return
        if (FirebaseApp.getInstance().options.projectId != "kirokun-dev" || packageName != "com.alchembright.kirokun.dev") return
        if (message.data["kirokunQA"] != "device-only-20261006") return
        File(filesDir, "kirokun-push-qa-received.json").writeText(JSONObject()
            .put("event", "received").put("projectId", "kirokun-dev").put("time", System.currentTimeMillis()).toString())
        KirokunNotifications.show(this, message.notification?.title ?: "KIROKUN Dev",
            message.notification?.body ?: "通知テスト", message.messageId ?: "dev-qa")
    }
}
