package com.example.bluetoothapp

import android.app.AlertDialog
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.content.ComponentName
import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.text.TextUtils
import android.util.Log
import android.widget.*
import androidx.appcompat.app.AppCompatActivity


class MainActivity : AppCompatActivity() {


    companion object {
        val EXTRA_ADDRESS: String = "Device_address"
    }

    private val ENABLED_NOTIFICATION_LISTENERS = "enabled_notification_listeners"
    private val ACTION_NOTIFICATION_LISTENER_SETTINGS = "android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS"
    private var enableNotificationListenerAlertDialog: AlertDialog? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val button:Button = findViewById(R.id.refresh_button)
        val devicesList:ListView= findViewById(R.id.devices_list_view)
        val bluetoothAdapter:BluetoothAdapter? = BluetoothAdapter.getDefaultAdapter()
        val REQUEST_ENABLE_BLUETOOTH = 0

        //check for Bluetooth functionality
        if(bluetoothAdapter == null){
            Toast.makeText(this, "bluetooth not available", Toast.LENGTH_SHORT).show()

        }
        else{
            Toast.makeText(this, "bluetooth available", Toast.LENGTH_SHORT).show()
        }

        // Check if Bluetooth is turned ON
        if(bluetoothAdapter?.isEnabled == false){
            val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BLUETOOTH)
            Log.i("BT-device", bluetoothAdapter.isEnabled.toString())
        }

        if(bluetoothAdapter?.isEnabled == true) {
            listPairedDevices(bluetoothAdapter, devicesList)
        }


        button.setOnClickListener {
            if (bluetoothAdapter != null) {
                listPairedDevices(bluetoothAdapter, devicesList)
            }
        }

        if(!isNotificationServiceEnabled()){
            enableNotificationListenerAlertDialog = buildNotificationServiceAlertDialog();
            enableNotificationListenerAlertDialog?.show();
        }


    }

    private fun isNotificationServiceEnabled(): Boolean {
        val pkgName = packageName
        val flat: String = Settings.Secure.getString(contentResolver,
                ENABLED_NOTIFICATION_LISTENERS)
        if (!TextUtils.isEmpty(flat)) {
            val names = flat.split(":").toTypedArray()
            for (i in names.indices) {
                val cn = ComponentName.unflattenFromString(names[i])
                if (cn != null) {
                    if (TextUtils.equals(pkgName, cn.packageName)) {
                        return true
                    }
                }
            }
        }
        return false
    }

    private fun buildNotificationServiceAlertDialog(): AlertDialog? {
        val alertDialogBuilder = AlertDialog.Builder(this)
        alertDialogBuilder.setTitle(R.string.notification_listener_service)
        alertDialogBuilder.setMessage(R.string.notification_listener_service_explanation)
        alertDialogBuilder.setPositiveButton(R.string.yes
        ) { dialog, id -> startActivity(Intent(ACTION_NOTIFICATION_LISTENER_SETTINGS)) }
        alertDialogBuilder.setNegativeButton(R.string.no
        ) { dialog, id ->
            // If you choose to not enable the notification listener
            // the app. will not work as expected
        }
        return alertDialogBuilder.create()
    }



     fun listPairedDevices(bluetoothAdapter: BluetoothAdapter, devicesList: ListView) {
         if (bluetoothAdapter?.isEnabled) {
             val pairedDevices: Set<BluetoothDevice>? = bluetoothAdapter?.bondedDevices
             val list : ArrayList<BluetoothDevice> = ArrayList()
             val listDevicesNames : ArrayList<String> = ArrayList()


             if (pairedDevices?.isNotEmpty()!!) {
                 pairedDevices?.forEach { device ->
                     val deviceName = device.name
                     val deviceHardwareAddress = device.address
                     list.add(device)
                     listDevicesNames.add(deviceName)
                 }
             }
             else {
                 Toast.makeText(this, "No paired bluetooth devices found", Toast.LENGTH_SHORT).show()
             }

             val adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, listDevicesNames)
             devicesList.adapter = adapter

             devicesList.onItemClickListener = AdapterView.OnItemClickListener { _, _, position, _ ->
                 val device: BluetoothDevice = list[position]
                 val address: String = device.address

                 val intent = Intent(this, ControlActivity::class.java)
                 intent.putExtra(EXTRA_ADDRESS, address)
                 startActivity(intent)
             }
         }


     }

    override fun onDestroy() {
        super.onDestroy()

    }
}



