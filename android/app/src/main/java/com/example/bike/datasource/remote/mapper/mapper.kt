package com.example.bike.datasource.remote.mapper

import com.example.bike.domain.model.Device
import com.example.bike.services.bluetooth.BluetoothClient

fun BluetoothClient.toDomain(): Device = Device(address = address, name = name)