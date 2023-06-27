package com.example.bluetoothapp

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.provider.Telephony
import android.util.Log
import android.widget.Toast
import androidx.annotation.RequiresApi
import com.example.bluetoothapp.AppsNotify.Companion.appIncommingNotification


class SmsReceiver : BroadcastReceiver() {


    companion object{
        var smsIncomingNotification:Boolean = false
    }
    @RequiresApi(Build.VERSION_CODES.KITKAT)
    override fun onReceive(context: Context?, intent: Intent) {
        val sendData = ControlActivity()
        var messageBody: String? = null
        var incomingNumber: String? = null
        if (Telephony.Sms.Intents.SMS_RECEIVED_ACTION == intent.action) {

            smsIncomingNotification = true
            for (smsMessage in Telephony.Sms.Intents.getMessagesFromIntent(intent)) {
                 messageBody = smsMessage.messageBody
                incomingNumber = smsMessage.originatingAddress
            }

            if (!incomingNumber.isNullOrEmpty()) {


                val name = sendData.searchName(incomingNumber)


                if(!CallReceiver.ringing){
                    if(name.equals(null)){
                        sendData.sendCommand("*$messageBody\n-from $incomingNumber\n")
                    }

                    else{
                        sendData.sendCommand("*$messageBody\n-from $name\n")
                    }
                }

//                sendData.sendCommand("-from $name\n")
                Log.d("Notify-sms", "$messageBody :from $name")
                smsIncomingNotification = false
            }
        }
        else{
            smsIncomingNotification = false
        }

    }
}