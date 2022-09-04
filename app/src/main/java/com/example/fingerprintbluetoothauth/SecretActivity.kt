package com.example.fingerprintbluetoothauth

import android.Manifest
import android.annotation.SuppressLint
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import java.util.*

class SecretActivity : AppCompatActivity() {
    @SuppressLint("MissingPermission")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_secret)

        val permission = ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT)

        if (permission != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.BLUETOOTH_CONNECT), 1)
        }

        val bluetoothManager = (getSystemService(Context.BLUETOOTH_SERVICE)) as BluetoothManager
        val adapter = bluetoothManager.getAdapter()
        val uuid = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB")
        val hc05 = adapter.getRemoteDevice("00:18:91:D6:D7:26") as BluetoothDevice
        val btsocket = hc05.createRfcommSocketToServiceRecord(uuid)
        btsocket.connect()

        val stringMac = "A39BEAB93885" //Dummy MAC address for development

        var macAdd = stringMac.encodeToByteArray() as ByteArray
        val sendStream = btsocket.getOutputStream()
        sendStream.write(macAdd)

        val lockButton = findViewById<Button>(R.id.lockBtn)
        val unlockButton = findViewById<Button>(R.id.unlockBtn)
        //val addUserButton = findViewById<Button>(R.id.addUser)


        lockButton.setOnClickListener{
            sendStream.write(115)//sends an 's'
        }
        unlockButton.setOnClickListener{
            sendStream.write(111)//sends an 'o'
        }


    }
}