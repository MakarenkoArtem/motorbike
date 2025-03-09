package com.example.bike.services.bluetooth

import android.app.Application
import android.bluetooth.BluetoothDevice
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.IBinder
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class BluetoothViewModel(application: Application) : AndroidViewModel(application) {
    var service: BluetoothService? = null

    private val serviceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, binder: IBinder?) {
            val localBinder = binder as BluetoothService.LocalBinder
            service = localBinder.getService()
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            service = null
        }
    }

    init{
        viewModelScope.launch{startBluetoothService()}
    }

    suspend fun waitForService() {
        while (service == null) {
            delay(100)
        }
    }

    suspend fun startBluetoothService() {
        val intent = Intent(getApplication(), BluetoothService::class.java)
        getApplication<Application>().bindService(
            intent, serviceConnection, Context.BIND_AUTO_CREATE
        )
        waitForService()
    }


    fun getRequiredPermissions() = service?.getRequiredPermissions() ?: emptyArray()

    fun checkBluetoothPermission() = service?.checkBluetoothPermission()
        ?: Result.failure(Exception("BluetoothService еще не привязан"))

    fun checkBluetoothAdapter() = service?.checkAdapter()
        ?: Result.failure(Exception("BluetoothService еще не привязан"))

    fun getDevicesFlow(): Result<StateFlow<List<BluetoothDevice>>> {
        val devices = service?.getDevicesFlow() ?: return Result.failure(Exception(""))
        return Result.success(devices)
    }

    fun getClient(device: BluetoothDevice) =
        service?.getClient(device) ?: Result.failure(Exception(""))
}