package kz.hashiroii.tiyin

import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import android.util.Log

class NotificationListener : NotificationListenerService() {

    companion object {
        const val TAG = "NotificationListener"

        var notificationCallback: ((StatusBarNotification) -> Unit)? = null
    }

    override fun onNotificationPosted(sbn: StatusBarNotification?) {
        super.onNotificationPosted(sbn)

        sbn?.let { notification ->
            Log.d(TAG, "Notification received from: ${notification.packageName}")
            Log.d(TAG, "Notification title: ${notification.notification.extras.getCharSequence("android.title")}")
            Log.d(TAG, "Notification text: ${notification.notification.extras.getCharSequence("android.text")}")

            notificationCallback?.invoke(notification)
        }
    }

    override fun onListenerConnected() {
        super.onListenerConnected()
        Log.d(TAG, "Notification listener connected")
    }
    override fun onListenerDisconnected() {
        super.onListenerDisconnected()
        Log.d(TAG, "Notification listener disconnected")
    }
}