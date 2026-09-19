package com.alchembright.dev.langtrackapp.util


import android.content.Intent
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.auth.FirebaseAuth
//import com.google.firebase.iid.FirebaseInstanceId
import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.alchembright.dev.langtrackapp.data.RepositoryFactory
import com.alchembright.dev.langtrackapp.screen.main.MainActivity

class MyFirebaseInstanceIDService: FirebaseMessagingService() {

    companion object {
        val MESSAGE_TEXT = "messageText"

        fun getDeviceTokengetDeviceToken(callback: (String?) -> Unit) {
            FirebaseMessaging.getInstance().token
                .addOnCompleteListener(OnCompleteListener { task ->
                    if (!task.isSuccessful) {
                        println("getDeviceToken ERROR ${task.exception?.localizedMessage}")
                        callback(null)
                        return@OnCompleteListener
                    }

                    // Get new Instance ID token
                    val token = task.result!!
                    callback(token)
                })
        }
    }

    override fun onNewToken(p0: String) {
        super.onNewToken(p0)
        RepositoryFactory.getRepository(this@MyFirebaseInstanceIDService).putDeviceToken()
    }

    override fun onMessageReceived(p0: RemoteMessage) {
        super.onMessageReceived(p0)

        println("messaging MyFirebaseInstanceIDService: onMessageReceived")
        //from google example
        /*
    // Not getting messages here? See why this may be: https://goo.gl/39bRNJ
    Log.d(TAG, "From: " + remoteMessage.getFrom());

    // Check if message contains a data payload.
    if (remoteMessage.getData().size() > 0) {
        Log.d(TAG, "Message data payload: " + remoteMessage.getData());

        if (/* Check if data needs to be processed by long running job */ true) {
            // For long-running tasks (10 seconds or more) use WorkManager.
            scheduleJob();
        } else {
            // Handle message within 10 seconds
            handleNow();
        }

    }

    // Check if message contains a notification payload.
    if (remoteMessage.getNotification() != null) {
        Log.d(TAG, "Message Notification Body: " + remoteMessage.getNotification().getBody());
    }

    // Also if you intend on generating your own notifications as a result of a received FCM
    // message, here is where that should be initiated. See sendNotification method below.*/

        if(FirebaseAuth.getInstance().currentUser != null) {
            RepositoryFactory.getRepository(this@MyFirebaseInstanceIDService).getAssignments()
            println("messaging MyFirebaseInstanceIDService: onMessageReceived, getAssignments")
        }
        println("messaging MyFirebaseInstanceIDService: onMessageReceived, body: ${p0.notification?.body}")

        val intent = Intent(this@MyFirebaseInstanceIDService, MainActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        for (i in p0.data){
            if (i.key == MESSAGE_TEXT){
                intent.putExtra(MESSAGE_TEXT, i.value)
            }
        }
        startActivity(intent)
    }
}