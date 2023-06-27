package com.example.bluetoothapp

import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Handler
import android.telephony.TelephonyManager
import android.util.Log
import android.widget.Toast
import java.text.SimpleDateFormat
import java.util.*


class CallReceiver : BroadcastReceiver() {

    companion object {
        var ringing:Boolean = false
    }
    override fun onReceive(context: Context?, intent: Intent?) {

        val sendData = ControlActivity()

//        try {
//            val state = intent!!.getStringExtra(TelephonyManager.EXTRA_STATE)
//            val incomingNumber = intent.getStringExtra(TelephonyManager.EXTRA_INCOMING_NUMBER)
//
//            Log.e("Incoming Number", "Number is ,$incomingNumber")
//            if (state == TelephonyManager.EXTRA_STATE_RINGING) {
//                Toast.makeText(context, "Incoming Call State", Toast.LENGTH_SHORT).show()
//
//                if(!incomingNumber.isNullOrEmpty()){
//                    Toast.makeText(context, "Ringing State Number is -$incomingNumber", Toast.LENGTH_SHORT).show()
//                }
//            }
//
//            if (state == TelephonyManager.EXTRA_STATE_OFFHOOK) {
//                Toast.makeText(context, "Call Received State", Toast.LENGTH_SHORT).show()
//            }
//            if (state == TelephonyManager.EXTRA_STATE_IDLE) {
//                Toast.makeText(context, "Call Idle State", Toast.LENGTH_SHORT).show()
//            }
//        } catch (e: Exception) {
//            e.printStackTrace()
//        }


//        val telephony = context!!.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
//        telephony.listen(object : PhoneStateListener() {
//
//            override fun onCallStateChanged(state: Int, incomingNumber: String) {
//                super.onCallStateChanged(state, incomingNumber)
//                val state = intent!!.getStringExtra(TelephonyManager.EXTRA_STATE)
//
//                if (state.equals(TelephonyManager.EXTRA_STATE_RINGING) ) {
//                Toast.makeText(context, "Incoming Call State", Toast.LENGTH_SHORT).show()
//                    println("incomingNumber : $incomingNumber")
//                Toast.makeText(context, "Ringing State Number is -$incomingNumber", Toast.LENGTH_SHORT).show()
//            }
//            if (state.equals( TelephonyManager.EXTRA_STATE_OFFHOOK)) {
//                Toast.makeText(context, "Call Received State", Toast.LENGTH_SHORT).show()
//            }
//            if (state.equals(TelephonyManager.EXTRA_STATE_IDLE)) {
//                Toast.makeText(context, "Call Idle State", Toast.LENGTH_SHORT).show()
//            }
//            }
//        }, PhoneStateListener.LISTEN_CALL_STATE)

        val incomingNumber: String? = intent!!.getStringExtra(TelephonyManager.EXTRA_INCOMING_NUMBER)

        if(intent?.getStringExtra(TelephonyManager.EXTRA_STATE).equals(TelephonyManager.EXTRA_STATE_OFFHOOK)){

            Log.d("Notify-call", "Call started")
            sendData.sendCommand("$")
            Toast.makeText(context, "Call started", Toast.LENGTH_LONG).show()
        }

        else if(intent?.getStringExtra(TelephonyManager.EXTRA_STATE).equals(TelephonyManager.EXTRA_STATE_IDLE)){
            
            ringing = false
            Log.d("Notify-call", "Call Ended")
            sendData.sendCommand("$")
            Toast.makeText(context, "Call Ended", Toast.LENGTH_LONG).show()
        }

        else if(intent?.getStringExtra(TelephonyManager.EXTRA_STATE).equals(TelephonyManager.EXTRA_STATE_RINGING)){

            ringing = true
            if (!incomingNumber.isNullOrEmpty()) {
                Log.d("debug","In Loop")
                val name = sendData.searchName(incomingNumber)

                    if (name.equals(null)) {

                        Log.d("Notify-call", incomingNumber)
                        sendData.sendCommand("# ${incomingNumber}\n")

                    } else {
                        Log.d("Notify-call", name.toString())
                        sendData.sendCommand("# ${name}\n")
                    }

                Toast.makeText(context, "Incoming Call..${name}", Toast.LENGTH_LONG).show()
            }
        }

    }
}