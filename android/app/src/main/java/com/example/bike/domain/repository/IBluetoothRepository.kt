package com.example.bike.domain.repository

import com.example.bike.datasource.remote.model.BluetoothData
import com.example.bike.domain.model.Device
import kotlinx.coroutines.flow.StateFlow

interface IBluetoothRepository {
    fun getStatus(): Result<Unit>
    fun getDevicesFlow(): Result<StateFlow<List<Device>>>
    fun getRequiredPermissions(): Array<String>
    fun checkBluetoothPermission(): Result<Unit>
    suspend fun connect(device: Device, check:Boolean=false): Result<StateFlow<BluetoothData>>
    fun getDevice(): Result<Device>
    fun getDataFlow() : Result<StateFlow<BluetoothData>>
    fun send(
        message: String,
        repeat: Int = 3,
        timeWait: Long = 100
    ): Result<Unit>

    fun getColors(): Result<List<Int>>
    suspend fun takeMessage(
        repeat: Int = 1,
        timeWait: Long = 200
    ): Result<String>
    suspend fun colorSend(
        color: Int,
        index: Int
    ): Result<Unit>

    suspend fun colorsSend(colors: List<Int>): Result<Unit>

    fun disconnect(): Result<Unit>
}