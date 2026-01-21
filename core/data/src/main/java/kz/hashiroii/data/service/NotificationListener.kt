package kz.hashiroii.data.service

import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import android.util.Log
import java.util.regex.Pattern

class NotificationListener : NotificationListenerService() {

    companion object {
        const val TAG = "NotificationListener"

        var notificationCallback: ((StatusBarNotification) -> Unit)? = null

        private val SUBSCRIPTION_PATTERN = Pattern.compile(
            "(?i)(subscription|подписка|абонемент|payment|платеж|оплата|billing|биллинг|" +
            "renewal|продление|charge|списание|charged|списано|paid|оплачено|" +
            "cost|стоимость|amount|сумма|price|цена|fee|плата|due|к оплате|" +
            "auto.?renew|автопродление|recurring|повторяющийся|monthly|ежемесячно|" +
            "annual|годовой|yearly|ежегодно|\\d+\\s*(₸|₽|\\$|€|£|тенге|рубл|dollar|euro))",
            Pattern.CASE_INSENSITIVE
        )
    }

    private fun isSubscriptionNotification(title: String?, text: String?): Boolean {
        val titleStr = title?.toString() ?: ""
        val textStr = text?.toString() ?: ""
        val combined = "$titleStr $textStr"
        return SUBSCRIPTION_PATTERN.matcher(combined).find()
    }

    override fun onNotificationPosted(sbn: StatusBarNotification?) {
        super.onNotificationPosted(sbn)

        sbn?.let { notification ->
            val title = notification.notification.extras.getCharSequence("android.title")
            val text = notification.notification.extras.getCharSequence("android.text")

            Log.d(TAG, "Notification received from: ${notification.packageName}")
            Log.d(TAG, "Notification title: $title")
            Log.d(TAG, "Notification text: $text")

            if (isSubscriptionNotification(title?.toString(), text?.toString())) {
                Log.d(TAG, "Subscription notification detected")
                notificationCallback?.invoke(notification)
            }
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