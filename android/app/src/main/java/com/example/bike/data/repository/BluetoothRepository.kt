package com.example.bike.data.repository

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.IBinder
import android.util.Log
import com.example.bike.datasource.remote.mapper.toDomain
import com.example.bike.datasource.remote.model.BluetoothData
import com.example.bike.domain.model.Device
import com.example.bike.domain.repository.IBluetoothRepository
import com.example.bike.services.bluetooth.BluetoothClient
import com.example.bike.services.bluetooth.BluetoothService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class BluetoothRepository(private val context: Context): IBluetoothRepository {
    var service: BluetoothService? = null
    private var client: BluetoothClient? = null
    private val serviceConnection: ServiceConnection

    init {
        serviceConnection = object: ServiceConnection {
            override fun onServiceConnected(
                name: ComponentName?,
                binder: IBinder?
            ) {
                val localBinder = binder as BluetoothService.LocalBinder
                service = localBinder.getService()
            }

            override fun onServiceDisconnected(name: ComponentName?) {
                service = null
            }
        }
        CoroutineScope(Dispatchers.Default).launch {
            repeat(6) {
                bindBluetoothService()
                delay(1000)
                if (service != null) {
                    service!!.getStatus()
                        .onFailure {
                            Log.d("Bike.BluetoothRepository", it.message ?: it.toString())
                        }
                    return@launch
                }
            }
        }
    }

    override fun getStatus() =
        service?.getStatus() ?: Result.failure(Exception("Bluetooth service not found"))

    private fun bindBluetoothService() {
        val intent = Intent(context.applicationContext, BluetoothService::class.java)
        val bound = context.applicationContext.bindService(
            intent, serviceConnection, Context.BIND_AUTO_CREATE
        )
        if (!bound) {
            Log.e("Bike.BluetoothRepository", "Failed to bind BluetoothService")
        }
    }

    override fun getDevicesFlow(): Result<StateFlow<List<Device>>> = kotlin.runCatching {
        service?.fetchDevicesFlow() ?: throw Exception("Device flow not exist")
    }

    override fun connect(
        device: Device,
        check: Boolean
    ): Result<StateFlow<BluetoothData>> {
        client?.disconnect()
        if (service == null) {
            return Result.failure(Exception("Bluetooth service not found"))
        }
        client = service!!.getClient(device)
            .getOrElse {
                return Result.failure(Exception("Bluetooth client not found"))
            }
        return client!!.connect(check)
    }

    override fun getDevice(): Result<Device> = kotlin.runCatching {
        if (client == null) {
            throw Exception("Device doesn't exist")
        }
        return@runCatching client!!.toDomain()
    }

    override fun getDataFlow(): Result<StateFlow<BluetoothData>> = kotlin.runCatching {
        if (client == null) {
            throw Exception("Device doesn't exist")
        }
        return@runCatching client!!.getDataFlow()
    }

    override fun send(
        message: String,
        repeat: Int,
        timeWait: Long
    ): Result<Unit> = client?.sendMessage(
        message, repeat, timeWait
    ) ?: Result.failure(Exception("Device doesn't exist"))

    suspend override fun takeMessage(
        repeat: Int,
        timeWait: Long
    ): Result<String> = client?.takeMessage(repeat, timeWait) ?: Result.failure(Exception("Device doesn't exist"))

    override fun getColors(): Result<List<Int>>  {
        return client?.getColors() ?: Result.failure(Exception("Device doesn't exist"))
    }

    override suspend fun colorSend(
        color: Int,
        index: Int
    ): Result<Unit> = client?.colorSend(color, index) ?: Result.failure(Exception("Device doesn't exist"))

    override suspend fun colorsSend(colors: List<Int>): Result<Unit> =
        client?.colorsSend(colors) ?: Result.failure(Exception("Device doesn't exist"))


    override fun getRequiredPermissions(): Array<String> =
        service?.getRequiredPermissions() ?: emptyArray()

    override fun checkBluetoothPermission(): Result<Unit> =
        service?.checkBluetoothPermission() ?: getStatus()

    override fun disconnect():Result<Unit> {
        val ans = client?.disconnect() ?: Result.success(Unit)
        client = null
        return ans
    }
}