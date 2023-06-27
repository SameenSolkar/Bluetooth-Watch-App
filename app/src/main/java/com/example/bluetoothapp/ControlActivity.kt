package com.example.bluetoothapp

import android.Manifest
import android.app.ProgressDialog
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothSocket
import android.content.Context
import android.content.pm.PackageManager
import android.database.Cursor
import android.os.AsyncTask
import android.os.Bundle
import android.provider.ContactsContract
import android.util.Log
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.bluetoothapp.AppsNotify.Companion.appIncommingNotification
import com.example.bluetoothapp.CallReceiver.Companion.ringing
import com.example.bluetoothapp.MainActivity.Companion.EXTRA_ADDRESS
import com.example.bluetoothapp.SmsReceiver.Companion.smsIncomingNotification
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*


class ControlActivity: AppCompatActivity() {

    companion object {
        var m_myUUID: UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB")
        var m_bluetoothSocket: BluetoothSocket? = null
        lateinit var m_progress: ProgressDialog
        lateinit var m_bluetoothAdapter: BluetoothAdapter
        var m_isConnected: Boolean = false
        lateinit var m_address: String
        var contactNameNumbers = mutableMapOf<String, String>()

//        var imageChangeBroadcastReceiver: ImageChangeBroadcastReceiver? = null
    }
    var cols = listOf<String>(
            ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME,
            ContactsContract.CommonDataKinds.Phone.NUMBER
    ).toTypedArray()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.control_layout)
        m_address = intent.getStringExtra(EXTRA_ADDRESS).toString()
        ConnectToDevice(this).execute()

        val control_led_on = findViewById<Button>(R.id.control_led_on)
        val control_led_off = findViewById<Button>(R.id.control_led_off)
        val control_led_disconnect = findViewById<Button>(R.id.control_led_disconnect)

        control_led_on.setOnClickListener { sendCommand("a") }
        control_led_off.setOnClickListener { sendCommand("b") }
        control_led_disconnect.setOnClickListener { disconnect() }

//        imageChangeBroadcastReceiver = ImageChangeBroadcastReceiver()
//        val intentFilter = IntentFilter()
//        intentFilter.addAction("com.example.bluetoothapp")
//        registerReceiver(imageChangeBroadcastReceiver, intentFilter)

        if(ContextCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE)
            != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(this,
                    arrayOf(Manifest.permission.READ_PHONE_STATE), 100);
        }

        if(ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CONTACTS)
                != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(this,
                    arrayOf(Manifest.permission.READ_CONTACTS), 100);
        }

        if(ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CALL_LOG)
                != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(this,
                    arrayOf(Manifest.permission.READ_CALL_LOG), 100);
        }

        if(ContextCompat.checkSelfPermission(this, Manifest.permission.RECEIVE_SMS)
                != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(this,
                    arrayOf(Manifest.permission.RECEIVE_SMS), 100);
        }


        readContacts()


    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if(requestCode == 100 && grantResults[0] == PackageManager.PERMISSION_GRANTED){

        }
        else{
            Toast.makeText(this, "Please grant all Permissions", Toast.LENGTH_LONG).show()
        }
    }

     fun readContacts(){
        val cursor: Cursor? = contentResolver.query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                cols, null, null, null)

        while (cursor?.moveToNext() == true){
            contactNameNumbers[cursor.getString(1)] = cursor.getString(0)

        }
         Log.d("contact", contactNameNumbers.toString())
    }

    fun searchName(number: String): String? {

        val name = contactNameNumbers.get(number)
        return name
    }

    fun sendCommand(input: String?) {


            if (m_bluetoothSocket != null) {
                try{
                    m_bluetoothSocket!!.outputStream.write(input?.toByteArray())
                } catch (e: IOException) {
                    e.printStackTrace()
                }
            }


    }

    private fun disconnect() {
        if (m_bluetoothSocket != null) {
            try {
                m_bluetoothSocket!!.close()
                m_bluetoothSocket = null
                m_isConnected = false
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
        finish()
    }

    private class ConnectToDevice(c: Context) : AsyncTask<Void, Void, String>() {
        private var connectSuccess: Boolean = true
        private val context: Context

        init {
            this.context = c
        }


        override fun onPreExecute() {
            super.onPreExecute()
            m_progress = ProgressDialog.show(context, "Connecting...", "please wait")
        }

        override fun doInBackground(vararg p0: Void?): String? {
            try {
                if (m_bluetoothSocket == null || !m_isConnected) {
                    m_bluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
                    val device: BluetoothDevice = m_bluetoothAdapter.getRemoteDevice(m_address)
                    m_bluetoothSocket = device.createInsecureRfcommSocketToServiceRecord(m_myUUID)
                    BluetoothAdapter.getDefaultAdapter().cancelDiscovery()
                    m_bluetoothSocket!!.connect()
                }
            } catch (e: IOException) {
                connectSuccess = false

                e.printStackTrace()
            }
            return null
        }

        override fun onPostExecute(result: String?) {
            super.onPostExecute(result)

            val controlActivity = ControlActivity()
            if (!connectSuccess) {
                Log.i("data", "couldn't connect")
                Toast.makeText(context, "Connection failed!! try again", Toast.LENGTH_SHORT).show()
            } else {
                m_isConnected = true

                Log.d("Notify-clockk", ringing.toString() + appIncommingNotification.toString() + smsIncomingNotification.toString())

//                    val handler = Handler()
//                    handler.postDelayed(object : Runnable {
//                        @SuppressLint("SimpleDateFormat")
//                        override fun run() {
//                            //Call your function here
//                            val currentTime = Calendar.getInstance().time
//                            val sdf = SimpleDateFormat("EEE dd/MMM hh:mm aa")
//                            val Datetime = sdf.format(currentTime)
//
//                            if(ringing || appIncommingNotification || smsIncomingNotification) {
//
//                            }
//
//                            else{
//                                Log.d("Notify-clockk", Datetime.toString())
//                                controlActivity.sendCommand("%$Datetime")
//                            }
//                        handler.postDelayed(this, 5000)//1 sec delay
//                        }
//                    }, 0)

//                val currentTime = Calendar.getInstance()
//                val EpochTime = currentTime.timeInMillis.div(1000)
//                val offset: Int = TimeZone.getDefault().rawOffset
//                val data = System.currentTimeMillis().div(1000);
//                val EpochTime = data

                val currentTime = Calendar.getInstance().time
                val sdf = SimpleDateFormat("HHmmssuddMMyy ")
                val Datetime = sdf.format(currentTime)

                Log.d("Notify-time", Datetime.toString())

                controlActivity.sendCommand("%$Datetime")

            }
            m_progress.dismiss()
        }


    }

//    class ImageChangeBroadcastReceiver : BroadcastReceiver() {
//        override fun onReceive(context: Context, intent: Intent) {
//            val receivedNotificationCode = intent.getIntExtra("Notification Code", -1)
//            val title = intent.getStringExtra("title")
//            val text = intent.getStringExtra("text")
//            val ticker = intent.getStringExtra("ticker")
//            val pack = intent.getStringExtra("pack")
//
////            Log.d("Notify-title", title.toString())
////            Log.d("Notify-text", text.toString())
////            Log.d("Notify-ticker", ticker.toString())
//////            Log.d("Notify-pack", pack.toString())
////            Log.d("Notify-num", receivedNotificationCode.toString())
//        }
//    }

}