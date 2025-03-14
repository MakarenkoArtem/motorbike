package com.example.bike.factory

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.bike.services.bluetooth.BluetoothViewModel
import com.example.bike.ui.viewmodel.ListDeviceDialogViewModel
import com.example.bike.ui.viewmodel.MainActivityViewModel

class MainActivityViewModelFactory(
    private val bluetoothViewModel: BluetoothViewModel,
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MainActivityViewModel::class.java)) {
            return MainActivityViewModel(bluetoothViewModel) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
