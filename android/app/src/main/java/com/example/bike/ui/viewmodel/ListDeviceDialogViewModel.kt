package com.example.bike.ui.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.bike.domain.model.Device
import com.example.bike.domain.model.DeviceStatus
import com.example.bike.domain.repository.IBluetoothRepository
import com.example.bike.model.ListDeviceViewData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class ListDeviceDialogViewModel(val bluetoothRepository: IBluetoothRepository): ViewModel() {
    private val _screenDataState: MutableStateFlow<ListDeviceViewData> =
        MutableStateFlow(ListDeviceViewData())
    var screenDataState: StateFlow<ListDeviceViewData> = _screenDataState

    init {
        viewModelScope.launch {
            var flowResult: Result<StateFlow<List<Device>>>
            do {
                flowResult = bluetoothRepository.getDevicesFlow()
                Log.d("Bike.ListDeviceVM", flowResult.toString())

                if (flowResult.isFailure) {
                    delay(1000)
                }
            } while (flowResult.isFailure)
            _screenDataState.value = _screenDataState.value.copy(bluetoothStatus = true)
            flowResult.getOrNull()
                ?.collect {devices ->
                    _screenDataState.value = _screenDataState.value.copy(devices = devices)
                }
        }
    }

    fun switchEvent(value: Boolean) {
        bluetoothRepository.getDevicesFlow()
        _screenDataState.value = _screenDataState.value.copy(bluetoothStatus = value)
    }

    fun getStatus() = bluetoothRepository.getStatus()

    fun checkBluetoothPermission() = bluetoothRepository.checkBluetoothPermission()

    private fun updateDeviceStatus(
        device: Device,
        status: DeviceStatus
    ){
        device.status=status
        _screenDataState.value =
            _screenDataState.value.copy(updateVar=!screenDataState.value.updateVar)
    }

    fun connect(curDevice: Device): Result<Unit> = kotlin.runCatching {
        updateDeviceStatus(curDevice, DeviceStatus.DISCOVERING)
        viewModelScope.launch(Dispatchers.IO) {
            Log.d("ListDeviceDialog", curDevice.toString())
            val state = bluetoothRepository.connect(device = curDevice)
                .getOrElse {
                    Log.d("ListDeviceDialog", it.message?:"Unknown error")
                    updateDeviceStatus(curDevice, DeviceStatus.NOTHING)
                    cancel()
                    return@launch
                }
            updateDeviceStatus(curDevice, DeviceStatus.CONNECTED)
            state.collect {status ->
                if (status.active) {
                    disconnect()
                    _screenDataState.value = _screenDataState.value.copy(
                        connectionStatus = true, connectedDevice = curDevice
                    )
                }
                cancel()
            }
        }
    }

    fun disconnect(): Result<Unit> {
        _screenDataState.value = _screenDataState.value.copy(
            connectionStatus = false, connectedDevice = null
        )
        return bluetoothRepository.disconnect()
    }
}