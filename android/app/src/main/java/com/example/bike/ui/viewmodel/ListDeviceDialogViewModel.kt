package com.example.bike.ui.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.bike.domain.model.Device
import com.example.bike.domain.model.DeviceStatus
import com.example.bike.domain.repository.IBluetoothRepository
import com.example.bike.model.ListDeviceViewData
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeoutOrNull

class ListDeviceDialogViewModel(val bluetoothRepository: IBluetoothRepository): ViewModel() {
    private val _screenDataState: MutableStateFlow<ListDeviceViewData> =
        MutableStateFlow(ListDeviceViewData())
    var screenDataState: StateFlow<ListDeviceViewData> = _screenDataState
    private var connectionScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    val _deviceQueue = MutableStateFlow(emptyList<Device>())

    init {
        viewModelScope.launch {
            var flowResult: Result<StateFlow<List<Device>>>
            do {
                flowResult = bluetoothRepository.getDevicesFlow()
                if (flowResult.isFailure) {
                    Log.d("ListDeviceDialogViewModel", flowResult.toString())
                    delay(1000)
                }
            } while (flowResult.isFailure)
            _screenDataState.value = _screenDataState.value.copy(bluetoothStatus = true)
            flowResult.getOrNull()
                ?.collect {devices ->
                    _screenDataState.value = _screenDataState.value.copy(devices = devices)
                }
        }
        connectionScope.launch {
            _deviceQueue.collect {devices ->
                if (devices.size > 0) {
                    createConnection(devices[0]).onFailure {
                        updateDeviceStatus(devices[0], DeviceStatus.NOTHING)
                    }
                    val list = _deviceQueue.value.toMutableList()
                    list.removeAt(0)
                    _deviceQueue.value = list.toList()
                }
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
    ) {
        device.status = status
        _screenDataState.value =
            _screenDataState.value.copy(updateVar = !screenDataState.value.updateVar)
    }

    suspend private fun createConnection(device: Device) = kotlin.runCatching {
        Log.d("ListDeviceDialogViewModel", device.toString())
        val state = bluetoothRepository.connect(device = device)
            .getOrElse {
                Log.d("ListDeviceDialogViewModel", it.message ?: "Unknown error")
                throw it
            }
        updateDeviceStatus(device, DeviceStatus.CONNECTED)
        val activeStatus = withTimeoutOrNull(5000L) {
            state.first {status ->
                if (status.active) {
                    disconnect()
                    _screenDataState.value = _screenDataState.value.copy(
                        connectionStatus = true, connectedDevice = device
                    )
                }
                status.active
            }
            connectionScope.cancel("Успешное подключение")
        }
    }

    fun connect(curDevice: Device): Result<Unit> = kotlin.runCatching {
        if (curDevice.status == DeviceStatus.NOTHING) {
            updateDeviceStatus(curDevice, DeviceStatus.DISCOVERING)
            _deviceQueue.value = _deviceQueue.value.toMutableList()
                .plus(curDevice)
                .toList()
        }
    }

    fun disconnect(): Result<Unit> {
        _screenDataState.value = _screenDataState.value.copy(
            connectionStatus = false, connectedDevice = null
        )
        connectionScope.cancel()
        return bluetoothRepository.disconnect()
    }
}