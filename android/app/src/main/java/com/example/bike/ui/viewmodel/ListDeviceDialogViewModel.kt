package com.example.bike.ui.viewmodel

import android.bluetooth.BluetoothDevice
import androidx.lifecycle.ViewModel
import com.example.bike.services.bluetooth.BluetoothClient
import com.example.bike.services.bluetooth.BluetoothViewModel

class ListDeviceDialogViewModel(private val bluetoothViewModel: BluetoothViewModel) : ViewModel() {
    var device: BluetoothClient? = null

    fun connect(curDevice: BluetoothDevice): Result<BluetoothClient> =
        bluetoothViewModel.getClient(device = curDevice)
}