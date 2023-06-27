package com.example.bluetoothapp

import android.annotation.SuppressLint
import android.app.Notification
import android.app.Notification.FLAG_GROUP_SUMMARY
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import android.util.Log
import androidx.annotation.RequiresApi
import com.example.bluetoothapp.ControlActivity.Companion.m_isConnected


@SuppressLint("OverrideAbstract")
@RequiresApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
class AppsNotify: NotificationListenerService() {

    var context: Context? = null
    override fun onCreate() {
        super.onCreate()
        context = applicationContext

    }

    companion object{
        var appIncommingNotification:Boolean = false
    }


    private object ApplicationPackageNames {
        const val FACEBOOK_PACK_NAME = "com.facebook.katana"
        const val FACEBOOK_MESSENGER_PACK_NAME = "com.facebook.orca"
        const val WHATSAPP_PACK_NAME = "com.whatsapp"
        const val INSTAGRAM_PACK_NAME = "com.instagram.android"
        const val SNAPCHAT_PACK_NAME = "com.snapchat.android"
    }

    /*
        These are the return codes we use in the method which intercepts
        the notifications, to decide whether we should do something or not
     */
    object InterceptedNotificationCode {
        const val FACEBOOK_CODE = 1
        const val WHATSAPP_CODE = 2
        const val INSTAGRAM_CODE = 3
        const val SNAPCHAT_CODE = 4
        const val OTHER_NOTIFICATIONS_CODE = 5 // We ignore all notification with code == 5
    }

    override fun onBind(intent: Intent?): IBinder? {
        return super.onBind(intent)
    }

    @Suppress("DEPRECATED_IDENTITY_EQUALS")
    @RequiresApi(Build.VERSION_CODES.KITKAT)
    override fun onNotificationPosted(sbn: StatusBarNotification) {
        val notificationCode = matchNotificationCode(sbn)
        val sendData = ControlActivity()

        val pack = sbn.packageName
        var ticker = ""
        if (sbn.notification.tickerText != null) {
            ticker = sbn.notification.tickerText.toString()
        }

        val extras = sbn.notification.extras
        val title = ""+extras.getCharSequence("android.title")
        val text = extras.getCharSequence("android.text").toString()

        if (sbn.notification.flags and FLAG_GROUP_SUMMARY !== 0) {
            //Ignore the notification
        }
        else {
            if (notificationCode != InterceptedNotificationCode.OTHER_NOTIFICATIONS_CODE) {
//            val intent = Intent("com.example.bluetoothapp")
//            intent.putExtra("Notification Code", notificationCode)
//            intent.putExtra("pack", pack)
//            intent.putExtra("ticker", ticker)
//            intent.putExtra("title", title)
//            intent.putExtra("text", text)
//            val receivedNotificationCode = intent.getIntExtra("Notification Code", -1)
//
//            sendBroadcast(intent)
                if(!CallReceiver.ringing) {

                    if (text.contains("Checking for new messages") || title.contains("Updating messages…") || title.contains("Running…") || title == "null" || text == "null") {

                        appIncommingNotification = false
                    } else {

                        appIncommingNotification = true
                        if (m_isConnected) {
                            Log.d("Notify-title", title)
                            Log.d("Notify-text", text)
//            Log.d("Notify-ticker", ticker.toString())
                            Log.d("Notify-pack", pack)
                            Log.d("Notify-num", notificationCode.toString())

                            sendData.sendCommand("$notificationCode$title: $text\n")
                        }

                    }
                }
            }
            appIncommingNotification = false
        }


    }

    override fun onNotificationRemoved(sbn: StatusBarNotification) {
        val notificationCode = matchNotificationCode(sbn)
        if (notificationCode != InterceptedNotificationCode.OTHER_NOTIFICATIONS_CODE) {
            val activeNotifications = this.activeNotifications
            if (activeNotifications != null && activeNotifications.size > 0) {
                for (i in activeNotifications.indices) {
                    if (notificationCode == matchNotificationCode(activeNotifications[i])) {
                        val intent = Intent("com.example.bluetoothapp")
                        intent.putExtra("Notification Code", notificationCode)
                        sendBroadcast(intent)
                        break
                    }
                }
            }
        }
    }

    private fun matchNotificationCode(sbn: StatusBarNotification): Int {
        val packageName = sbn.packageName
        return if (packageName == ApplicationPackageNames.FACEBOOK_PACK_NAME || packageName == ApplicationPackageNames.FACEBOOK_MESSENGER_PACK_NAME) {
            InterceptedNotificationCode.FACEBOOK_CODE
        } else if (packageName == ApplicationPackageNames.INSTAGRAM_PACK_NAME) {
            InterceptedNotificationCode.INSTAGRAM_CODE
        } else if (packageName == ApplicationPackageNames.WHATSAPP_PACK_NAME) {
            InterceptedNotificationCode.WHATSAPP_CODE
        } else if (packageName == ApplicationPackageNames.SNAPCHAT_PACK_NAME) {
            InterceptedNotificationCode.SNAPCHAT_CODE

        } else {
            InterceptedNotificationCode.OTHER_NOTIFICATIONS_CODE
        }
    }
}