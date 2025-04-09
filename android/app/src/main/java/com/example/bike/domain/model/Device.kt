package com.example.bike.domain.model

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

enum class DeviceStatus {
    NOTHING, WAITING, DISCOVERING, CONNECTED
}

@Parcelize
data class Device(
    val address: String,
    val name: String,
    var status: DeviceStatus = DeviceStatus.NOTHING
): Parcelable {
    fun sameDevice(device: Device?): Boolean {
        return !(device == null || address != device.address)
    }
}