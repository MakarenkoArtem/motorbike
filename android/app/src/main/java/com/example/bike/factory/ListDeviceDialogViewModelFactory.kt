package com.example.bike.factory

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.bike.services.bluetooth.BluetoothViewModel
import com.example.bike.ui.viewmodel.ListDeviceDialogViewModel

class ListDeviceDialogViewModelFactory(
    private val bluetoothViewModel: BluetoothViewModel,
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ListDeviceDialogViewModel::class.java)) {//проверка являестя ли modelClass предком ListDeviceDialogViewModel
            return ListDeviceDialogViewModel(bluetoothViewModel) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
