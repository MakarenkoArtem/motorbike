package com.example.bike.model

import com.example.bike.domain.model.Device

data class ListDeviceViewData(
    val devices: List<Device> = emptyList(),
    val bluetoothStatus: Boolean = false,
    val connectionStatus: Boolean = false,
    val connectedDevice: Device?=null
) {}