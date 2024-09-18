package com.example.bike.screens.DialogView

import android.app.Activity
import android.bluetooth.BluetoothDevice
import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import com.example.bike.BT.BTClient
import com.example.bike.BT.BTService

class ListDeviceDialogViewModel(
    val context: Context,
    val activity: Activity
) : ViewModel() {
    val btService: BTService = BTService(context = context, activity = activity)

    //val devices = btService.getPairedDevicesFlow()
    var devices = btService.getPairedDevices()
    var device: BTClient? = null

    fun connect(curDevice: BluetoothDevice): Result<Unit> {
        device = btService.getClient(device = curDevice)
        return device!!.connect()
    }

    fun disconnect(): Result<Unit> {
        Log.d("BikeBluetooth", "???")
        return device?.disconnect() ?: Result.success(Unit)
    }
    /*
        fun getPairedDevices(): StateFlow<List<BluetoothDevice>> {
            _listDevicesState.value = btService.getPairedDevices()
            Log.d("myLog", listDevicesState.value.toString())
            return listDevicesState
        }*/
}